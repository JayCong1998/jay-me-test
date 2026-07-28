package com.jaymetest.service;

import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.model.enums.AbyssLevel;
import com.jaymetest.model.enums.FanLevel;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.GameStrategy;
import com.jaymetest.service.game.GameStrategyFactory;
import com.jaymetest.service.game.LevelInfo;
import com.jaymetest.service.game.PostSubmitHook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

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
        request.setMode(GameMode.CLASSIC);

        GameResultDTO result = statsService.submitResult(request);

        assertNotNull(result);
        assertEquals(70, result.getScore());
        assertEquals(7, result.getCorrectCount());
        assertEquals(10, result.getTotalQuestions());
        assertEquals(FanLevel.SENIOR.name(), result.getLevel());
        assertEquals(FanLevel.SENIOR.getTitle(), result.getLevelTitle());
        assertEquals("test-uuid", result.getRoundId());
        assertEquals(GameMode.CLASSIC, result.getMode());
        assertNull(result.getAlbumKey());
        assertFalse(result.isUsedRevival());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void submitAbyssResultKeepsGuestRecordButNormalizesRevival() {
        when(strategyFactory.get(GameMode.ABYSS)).thenReturn(gameStrategy);
        when(gameStrategy.calculateScore(12, 13)).thenReturn(12);
        when(gameStrategy.evaluateLevel(12)).thenReturn(AbyssLevel.ABYSS_KNIGHT);
        when(gameStrategy.getPostSubmitHooks()).thenReturn(List.of());
        when(gameRecordMapper.selectOne(any())).thenReturn(null);
        when(gameRecordMapper.insert(any(GameRecord.class))).thenReturn(1);
        when(gameRecordMapper.countTotal()).thenReturn(101L);
        when(gameRecordMapper.countByCorrectCountLessThan(12)).thenReturn(90L);

        GameSubmitRequest request = new GameSubmitRequest();
        request.setRoundId("abyss-round");
        request.setMode(GameMode.ABYSS);
        request.setCorrectCount(12);
        request.setTotalQuestions(13);
        request.setTimeSpentSecs(180);
        request.setUsedRevival(1);

        GameResultDTO result = statsService.submitResult(request);

        assertEquals("abyss-round", result.getRoundId());
        assertEquals(GameMode.ABYSS, result.getMode());
        assertEquals(12, result.getScore());
        assertFalse(result.isUsedRevival());

        ArgumentCaptor<GameRecord> recordCaptor = ArgumentCaptor.forClass(GameRecord.class);
        verify(gameRecordMapper).insert(recordCaptor.capture());
        GameRecord stored = recordCaptor.getValue();
        assertNull(stored.getUserId());
        assertEquals(GameMode.ABYSS.name(), stored.getMode());
        assertEquals(0, stored.getUsedRevival());
        assertNotNull(stored.getCreatedAt());
    }
}
