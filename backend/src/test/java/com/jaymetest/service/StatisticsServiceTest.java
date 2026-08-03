package com.jaymetest.service;

import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.config.LevelRangeProperties;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.dto.StatsOverviewDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatisticsServiceTest {

    @Test
    void overviewUsesRealUserCountAndMaxAbyssStreakForHomeStats() {
        GameRecordMapper gameRecordMapper = mock(GameRecordMapper.class);
        UserMapper userMapper = mock(UserMapper.class);
        ClassicGameProperties classicGameProperties = new ClassicGameProperties();
        classicGameProperties.setQuestionCount(20);
        classicGameProperties.setLevels(List.of(
                level("PASSERBY", 0, 29),
                level("ULTIMATE", 90, 100)
        ));

        when(gameRecordMapper.countTotalByRegisteredUsers()).thenReturn(32L);
        when(userMapper.countTotalUsers()).thenReturn(4L);
        when(gameRecordMapper.selectAverageScoreByRegisteredUsers()).thenReturn(72.34);
        when(gameRecordMapper.selectMaxAbyssStreakByRegisteredUsers()).thenReturn(18);
        when(gameRecordMapper.selectLevelDistributionByRegisteredUsers()).thenReturn(List.of(
                Map.of("correct_count", 4, "cnt", 2L),
                Map.of("correct_count", 20, "cnt", 1L)
        ));

        StatisticsService statisticsService = new StatisticsService(
                gameRecordMapper, userMapper, classicGameProperties);

        StatsOverviewDTO overview = statisticsService.getOverview();

        assertEquals(32L, overview.getTotalGames());
        assertEquals(4L, overview.getTotalPlayers());
        assertEquals(72.3, overview.getAverageScore());
        assertEquals(18, overview.getMaxAbyssStreak());
    }

    private static LevelRangeProperties level(String key, int min, int max) {
        LevelRangeProperties level = new LevelRangeProperties();
        level.setKey(key);
        level.setTitle(key);
        level.setDescription(key);
        level.setMin(min);
        level.setMax(max);
        return level;
    }
}
