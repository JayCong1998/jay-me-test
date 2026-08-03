package com.jaymetest.service;

import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.dto.StatsOverviewDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final GameRecordMapper gameRecordMapper;
    private final UserMapper userMapper;
    private final ClassicGameProperties classicGameProperties;

    public StatsOverviewDTO getOverview() {
        long totalGames = gameRecordMapper.countTotalByRegisteredUsers();
        Map<String, Double> levelDistribution = new HashMap<>();
        List<Map<String, Object>> distribution = gameRecordMapper.selectLevelDistributionByRegisteredUsers();
        for (var level : classicGameProperties.getLevels()) {
            long count = 0;
            for (Map<String, Object> row : distribution) {
                int correctCount = ((Number) row.get("correct_count")).intValue();
                int accuracy = correctCount * 100 / classicGameProperties.getQuestionCount();
                if (accuracy >= level.getMin() && accuracy <= level.getMax()) count += ((Number) row.get("cnt")).longValue();
            }
            levelDistribution.put(level.getKey(), totalGames > 0 ? Math.round(((double) count / totalGames) * 10000.0) / 100.0 : 0.0);
        }
        return StatsOverviewDTO.builder().totalPlayers(userMapper.countTotalUsers()).totalGames(totalGames)
                .averageScore(Math.round(gameRecordMapper.selectAverageScoreByRegisteredUsers() * 10.0) / 10.0)
                .maxAbyssStreak(gameRecordMapper.selectMaxAbyssStreakByRegisteredUsers())
                .levelDistribution(levelDistribution).build();
    }
}
