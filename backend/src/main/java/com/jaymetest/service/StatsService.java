package com.jaymetest.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.GameRecordDTO;
import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;
import com.jaymetest.model.dto.StatsOverviewDTO;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.model.enums.FanLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务：等级计算 + 百分位
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final GameRecordMapper gameRecordMapper;

    /**
     * 提交游戏结果
     */
    public GameResultDTO submitResult(GameSubmitRequest request) {
        // 检查 roundId 是否已提交（去重）
        GameRecord existing = gameRecordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GameRecord>()
                        .eq(GameRecord::getRoundId, request.getRoundId()));
        if (existing != null) {
            throw new BusinessException(400, "该 roundId 已提交过结果");
        }

        int totalQuestions = 10;
        int correctCount = request.getCorrectCount();
        int score = correctCount * 10;
        double accuracy = (double) correctCount / totalQuestions;

        // 计算等级
        FanLevel level = FanLevel.fromScore(correctCount);

        // 判断是否登录：登录用户写入 user_id
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception ignored) {
            // 游客，userId 保持 null
        }

        // 保存记录
        GameRecord record = new GameRecord();
        record.setRoundId(request.getRoundId());
        record.setUserId(userId);
        record.setNickname(request.getNickname() != null ? request.getNickname() : "匿名杰迷");
        record.setTotalQuestions(totalQuestions);
        record.setCorrectCount(correctCount);
        record.setTimeSpentSecs(request.getTimeSpentSecs());
        record.setUsedRevival(request.getUsedRevival() != null ? request.getUsedRevival() : 0);
        gameRecordMapper.insert(record);

        // 计算百分位
        long totalPlayers = gameRecordMapper.countTotal();
        long lowerCount = gameRecordMapper.countByCorrectCountLessThan(correctCount);
        double beatPercentage = totalPlayers > 0
                ? Math.round(((double) lowerCount / totalPlayers) * 10000.0) / 100.0
                : 0.0;
        beatPercentage = Math.min(beatPercentage, 99.99);

        log.info("游戏结果已保存 roundId={}, userId={}, correctCount={}, level={}",
                request.getRoundId(), userId, correctCount, level.name());

        return GameResultDTO.builder()
                .score(score)
                .correctCount(correctCount)
                .totalQuestions(totalQuestions)
                .accuracy(accuracy)
                .timeSpentSecs(request.getTimeSpentSecs())
                .level(level.name())
                .levelTitle(level.getTitle())
                .levelDescription(level.getDescription())
                .beatPercentage(beatPercentage)
                .totalPlayers(totalPlayers)
                .build();
    }

    /**
     * 全局统计概览
     */
    public StatsOverviewDTO getOverview() {
        long totalGames = gameRecordMapper.countTotal();
        double averageScore = gameRecordMapper.selectAverageScore();

        // 计算等级分布
        Map<String, Double> levelDistribution = new HashMap<>();
        List<Map<String, Object>> distribution = gameRecordMapper.selectLevelDistribution();
        for (FanLevel level : FanLevel.values()) {
            long count = 0;
            for (Map<String, Object> row : distribution) {
                int correctCount = ((Number) row.get("correct_count")).intValue();
                if (correctCount >= level.getMinScore() && correctCount <= level.getMaxScore()) {
                    count += ((Number) row.get("cnt")).longValue();
                }
            }
            double pct = totalGames > 0
                    ? Math.round(((double) count / totalGames) * 10000.0) / 100.0
                    : 0.0;
            levelDistribution.put(level.name(), pct);
        }

        return StatsOverviewDTO.builder()
                .totalPlayers(totalGames) // 每个记录视为一个玩家（匿名）
                .totalGames(totalGames)
                .averageScore(Math.round(averageScore * 10.0) / 10.0)
                .levelDistribution(levelDistribution)
                .build();
    }

    /**
     * 获取当前登录用户的考试记录
     */
    public List<GameRecordDTO> getMyRecords() {
        long userId = StpUtil.getLoginIdAsLong();
        List<GameRecord> records = gameRecordMapper.selectByUserId(userId, 20);
        return records.stream().map(r -> {
            FanLevel level = FanLevel.fromScore(r.getCorrectCount());
            return GameRecordDTO.builder()
                    .roundId(r.getRoundId())
                    .correctCount(r.getCorrectCount())
                    .totalQuestions(r.getTotalQuestions())
                    .timeSpentSecs(r.getTimeSpentSecs())
                    .usedRevival(r.getUsedRevival() != null && r.getUsedRevival() == 1)
                    .createdAt(r.getCreatedAt())
                    .level(level.name())
                    .levelTitle(level.getTitle())
                    .build();
        }).collect(Collectors.toList());
    }
}
