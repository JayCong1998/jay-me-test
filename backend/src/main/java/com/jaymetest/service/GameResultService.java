package com.jaymetest.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.GameRoundCache;
import com.jaymetest.service.game.GameStrategy;
import com.jaymetest.service.game.GameStrategyFactory;
import com.jaymetest.service.game.LevelInfo;
import com.jaymetest.service.game.PostSubmitHook;
import com.jaymetest.service.game.RoundCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** 游戏结算服务：校验 Round、持久化成绩并执行玩法后置处理。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameResultService {
    private final GameRecordMapper gameRecordMapper;
    private final GameStrategyFactory strategyFactory;
    private final RoundCacheManager roundCacheManager;

    public GameResultDTO submitResult(GameSubmitRequest request) {
        GameRecord existing = gameRecordMapper.selectOne(new LambdaQueryWrapper<GameRecord>()
                .eq(GameRecord::getRoundId, request.getRoundId()));
        if (existing != null) throw new BusinessException(400, "该 roundId 已提交过结果");

        GameRoundCache round = roundCacheManager.getOrThrow(request.getRoundId());
        GameMode mode = round.getMode();
        GameStrategy strategy = strategyFactory.get(mode);
        int totalQuestions = mode == GameMode.ABYSS ? round.getAnsweredCount() : round.getAnswerMap().size();
        int correctCount = round.getCorrectCount();
        if (mode == GameMode.ABYSS) round.requireAbyssFailed();
        if (mode != GameMode.ABYSS && round.getAnsweredCount() != totalQuestions) {
            throw new BusinessException(400, "本局尚未完成，不能提交结果");
        }

        int score = strategy.calculateScore(correctCount, totalQuestions);
        double accuracy = totalQuestions > 0 ? (double) correctCount / totalQuestions : 0.0;
        LevelInfo levelInfo = strategy.evaluateLevel(correctCount);
        Long userId = null;
        try { userId = StpUtil.getLoginIdAsLong(); } catch (Exception ignored) { }

        int usedRevival = round.getRevivalUsed();
        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
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

        long totalPlayers = gameRecordMapper.countTotal();
        long lowerCount = gameRecordMapper.countByCorrectCountLessThan(correctCount);
        double beatPercentage = totalPlayers > 0 ? Math.round(((double) lowerCount / totalPlayers) * 10000.0) / 100.0 : 0.0;
        GameResultDTO.GameResultDTOBuilder builder = GameResultDTO.builder()
                .roundId(request.getRoundId()).mode(mode).albumKey(round.getAlbumKey())
                .score(score).correctCount(correctCount).totalQuestions(totalQuestions).accuracy(accuracy)
                .timeSpentSecs(request.getTimeSpentSecs()).usedRevival(usedRevival > 0)
                .createdAt(createdAt.toString()).level(levelInfo.name()).levelTitle(levelInfo.getTitle())
                .levelDescription(levelInfo.getDescription()).beatPercentage(Math.min(beatPercentage, 99.99))
                .totalPlayers(totalPlayers);
        for (PostSubmitHook hook : strategy.getPostSubmitHooks()) {
            hook.afterSubmit(round.getAlbumKey(), correctCount, totalQuestions, builder, userId);
        }
        GameResultDTO result = builder.build();
        roundCacheManager.remove(request.getRoundId());
        log.info("游戏结果已保存 roundId={}, userId={}, mode={}, correctCount={}, level={}",
                request.getRoundId(), userId, mode, correctCount, levelInfo.name());
        return result;
    }
}
