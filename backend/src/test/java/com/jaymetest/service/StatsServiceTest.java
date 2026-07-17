package com.jaymetest.service;

import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;
import com.jaymetest.model.enums.FanLevel;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.GameStrategy;
import com.jaymetest.service.game.GameStrategyFactory;
import com.jaymetest.service.game.LevelInfo;
import com.jaymetest.service.game.PostSubmitHook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private GameRecordMapper gameRecordMapper;

    @Mock
    private GameStrategyFactory strategyFactory;

    @Mock
    private GameStrategy gameStrategy;

    @InjectMocks
    private StatsService statsService;

    @Test
    void testSubmitResult_classicMode() {
        // 工厂和策略 Mock
        when(strategyFactory.resolveMode("CLASSIC")).thenReturn(GameMode.CLASSIC);
        when(strategyFactory.get(GameMode.CLASSIC)).thenReturn(gameStrategy);
        when(gameStrategy.calculateScore(7, 10)).thenReturn(70);
        when(gameStrategy.evaluateLevel(7)).thenReturn(FanLevel.SENIOR);
        when(gameStrategy.getPostSubmitHooks()).thenReturn(List.of());

        // Mapper Mock
        when(gameRecordMapper.selectOne(any())).thenReturn(null);
        when(gameRecordMapper.insert(any(com.jaymetest.model.entity.GameRecord.class))).thenReturn(1);
        when(gameRecordMapper.countTotal()).thenReturn(100L);
        when(gameRecordMapper.countByCorrectCountLessThan(eq(7))).thenReturn(65L);

        GameSubmitRequest request = new GameSubmitRequest();
        request.setRoundId("test-uuid");
        request.setCorrectCount(7);
        request.setTimeSpentSecs(120);
        request.setUsedRevival(0);

        GameResultDTO result = statsService.submitResult(request);

        assertNotNull(result);
        assertEquals(70, result.getScore());
        assertEquals(7, result.getCorrectCount());
        assertEquals(10, result.getTotalQuestions());
        assertEquals(FanLevel.SENIOR.name(), result.getLevel());
        assertEquals(FanLevel.SENIOR.getTitle(), result.getLevelTitle());
    }
}
