package com.jaymetest.service;

import com.jaymetest.config.AlbumGameProperties;
import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.LeaderboardEntry;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.ConfiguredLevel;
import com.jaymetest.service.game.GameStrategy;
import com.jaymetest.service.game.GameStrategyFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaderboardServiceTest {

    @Test
    void displaysTheConfiguredClassicQuestionCountWithoutChangingCorrectCountRanking() {
        ClassicGameProperties classicProperties = new ClassicGameProperties();
        classicProperties.setQuestionCount(20);
        GameStrategyFactory strategyFactory = mock(GameStrategyFactory.class);
        GameStrategy classicStrategy = mock(GameStrategy.class);
        when(strategyFactory.get(GameMode.CLASSIC)).thenReturn(classicStrategy);
        when(classicStrategy.evaluateLevel(12)).thenReturn(new ConfiguredLevel("SENIOR", "资深杰迷", "desc"));

        LeaderboardService service = new LeaderboardService(
                mock(GameRecordMapper.class), strategyFactory, new AlbumGameProperties(), classicProperties);
        List<LeaderboardEntry> entries = ReflectionTestUtils.invokeMethod(
                service,
                "mapClassicEntries",
                List.of(Map.of("rank", 1L, "nickname", "Jay", "correctCount", 12, "timeSpentSecs", 60)));

        assertEquals(12, entries.getFirst().getCorrectCount());
        assertEquals("12/20", entries.getFirst().getScoreText());
    }
}
