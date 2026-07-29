package com.jaymetest.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.GameRecordDTO;
import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;
import com.jaymetest.model.dto.StatsOverviewDTO;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.GameStrategy;
import com.jaymetest.service.game.GameStrategyFactory;
import com.jaymetest.service.game.GameRecordDTOAssembler;
import com.jaymetest.service.game.LevelInfo;
import com.jaymetest.service.game.PostSubmitHook;
import com.jaymetest.service.game.RoundCacheManager;
import com.jaymetest.service.game.GameRoundCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务：等级计算 + 百分位 + 结果持久化。
 *
 * <p>重构后通过 {@link GameStrategyFactory} 获取策略，所有模式分支由多态处理。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final GameRecordMapper gameRecordMapper;
    private final GameStrategyFactory strategyFactory;
    private final GameRecordDTOAssembler gameRecordDTOAssembler;
    private final RoundCacheManager roundCacheManager;
    private final ClassicGameProperties classicGameProperties;

    /**
     * 提交游戏结果 — 模式差异完全委托给策略。
     */
    public GameResultDTO submitResult(GameSubmitRequest request) {
        // roundId 是服务端生成的回合凭证，先去重可以挡住刷新页面或重复请求造成的二次入榜。
        GameRecord existing = gameRecordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GameRecord>()
                        .eq(GameRecord::getRoundId, request.getRoundId()));
        if (existing != null) {
            throw new BusinessException(400, "该 roundId 已提交过结果");
        }

        GameRoundCache round = roundCacheManager.getOrThrow(request.getRoundId());
        GameMode mode = round.getMode();
        GameStrategy strategy = strategyFactory.get(mode);
        int totalQuestions = mode == GameMode.ABYSS
                ? round.getAnsweredCount()
                : round.getAnswerMap().size();
        int correctCount = round.getCorrectCount();
        if (mode != GameMode.ABYSS && round.getAnsweredCount() != totalQuestions) {
            throw new BusinessException(400, "本局尚未完成，不能提交结果");
        }

        // 计分 & 等级 — 委托给策略
        int score = strategy.calculateScore(correctCount, totalQuestions);
        double accuracy = totalQuestions > 0 ? (double) correctCount / totalQuestions : 0.0;
        LevelInfo levelInfo = strategy.evaluateLevel(correctCount);

        // 提交成绩对游客开放；拿不到登录态时保留匿名记录，不影响经典模式分享链路。
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception ignored) {
        }

        // 深渊模式没有复活规则，强制归零避免前端旧状态污染记录。
        int usedRevival = mode == GameMode.ABYSS ? 0 : round.getRevivalUsed();
        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        // 持久化
        GameRecord record = new GameRecord();
        record.setRoundId(request.getRoundId());
        record.setMode(mode.name());
        record.setAlbumKey(round.getAlbumKey());
        record.setUserId(userId);
        record.setNickname(request.getNickname() != null ? request.getNickname() : "匿名杰迷");
        record.setTotalQuestions(totalQuestions);
        record.setCorrectCount(correctCount);
        record.setTimeSpentSecs(request.getTimeSpentSecs());
        record.setUsedRevival(usedRevival);
        record.setCreatedAt(createdAt);
        gameRecordMapper.insert(record);

        // 百分位只比较答对数，保留 99.99 上限避免展示成“超过 100% 玩家”。
        long totalPlayers = gameRecordMapper.countTotal();
        long lowerCount = gameRecordMapper.countByCorrectCountLessThan(correctCount);
        double beatPercentage = totalPlayers > 0
                ? Math.round(((double) lowerCount / totalPlayers) * 10000.0) / 100.0
                : 0.0;
        beatPercentage = Math.min(beatPercentage, 99.99);

        log.info("游戏结果已保存 roundId={}, userId={}, mode={}, correctCount={}, level={}",
                request.getRoundId(), userId, mode, correctCount, levelInfo.name());

        // 构建结果（通用字段）
        GameResultDTO.GameResultDTOBuilder builder = GameResultDTO.builder()
                .roundId(request.getRoundId())
                .mode(mode)
                .albumKey(round.getAlbumKey())
                .score(score)
                .correctCount(correctCount)
                .totalQuestions(totalQuestions)
                .accuracy(accuracy)
                .timeSpentSecs(request.getTimeSpentSecs())
                .usedRevival(usedRevival == 1)
                .createdAt(createdAt.toString())
                .level(levelInfo.name())
                .levelTitle(levelInfo.getTitle())
                .levelDescription(levelInfo.getDescription())
                .beatPercentage(beatPercentage)
                .totalPlayers(totalPlayers);

        // 执行后置钩子（如专辑解锁）
        for (PostSubmitHook hook : strategy.getPostSubmitHooks()) {
            hook.afterSubmit(round.getAlbumKey(), correctCount, totalQuestions, builder, userId);
        }
        GameResultDTO result = builder.build();
        roundCacheManager.remove(request.getRoundId());
        return result;
    }

    /**
     * 全局统计概览
     */
    public StatsOverviewDTO getOverview() {
        long totalGames = gameRecordMapper.countTotal();
        double averageScore = gameRecordMapper.selectAverageScore();

        Map<String, Double> levelDistribution = new HashMap<>();
        List<Map<String, Object>> distribution = gameRecordMapper.selectLevelDistribution();
        for (var level : classicGameProperties.getLevels()) {
            long count = 0;
            for (Map<String, Object> row : distribution) {
                int correctCount = ((Number) row.get("correct_count")).intValue();
                int accuracy = correctCount * 100 / classicGameProperties.getQuestionCount();
                if (accuracy >= level.getMin() && accuracy <= level.getMax()) {
                    count += ((Number) row.get("cnt")).longValue();
                }
            }
            double pct = totalGames > 0
                    ? Math.round(((double) count / totalGames) * 10000.0) / 100.0
                    : 0.0;
            levelDistribution.put(level.getKey(), pct);
        }

        return StatsOverviewDTO.builder()
                .totalPlayers(totalGames)
                .totalGames(totalGames)
                .averageScore(Math.round(averageScore * 10.0) / 10.0)
                .levelDistribution(levelDistribution)
                .build();
    }

    /**
     * 获取当前登录用户的考试记录（分页）
     */
    public List<GameRecordDTO> getMyRecords(int page, int size) {
        long userId = StpUtil.getLoginIdAsLong();
        int offset = (page - 1) * size;
        List<GameRecord> records = gameRecordMapper.selectByUserId(userId, size, offset);
        return records.stream()
                .map(gameRecordDTOAssembler::toDTO)
                .collect(Collectors.toList());
    }
}
