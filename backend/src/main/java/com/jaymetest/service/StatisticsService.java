package com.jaymetest.service;

import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.mapper.GameRecordMapper;
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
    private final ClassicGameProperties classicGameProperties;

    public StatsOverviewDTO getOverview() {
        long totalGames = gameRecordMapper.countTotal();
        Map<String, Double> levelDistribution = new HashMap<>();
        List<Map<String, Object>> distribution = gameRecordMapper.selectLevelDistribution();
        for (var level : classicGameProperties.getLevels()) {
            long count = 0;
            for (Map<String, Object> row : distribution) {
                int correctCount = ((Number) row.get("correct_count")).intValue();
                int accuracy = correctCount * 100 / classicGameProperties.getQuestionCount();
                if (accuracy >= level.getMin() && accuracy <= level.getMax()) count += ((Number) row.get("cnt")).longValue();
            }
            levelDistribution.put(level.getKey(), totalGames > 0 ? Math.round(((double) count / totalGames) * 10000.0) / 100.0 : 0.0);
        }
        return StatsOverviewDTO.builder().totalPlayers(totalGames).totalGames(totalGames)
                .averageScore(Math.round(gameRecordMapper.selectAverageScore() * 10.0) / 10.0)
                .levelDistribution(levelDistribution).build();
    }
}
