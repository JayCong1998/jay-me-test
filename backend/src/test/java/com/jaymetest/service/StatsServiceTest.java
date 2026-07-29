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
import com.jaymetest.service.game.RoundCacheManager;
import com.jaymetest.service.game.GameRoundCache;
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

    @Mock
    private RoundCacheManager roundCacheManager;

    @InjectMocks
    private StatsService statsService;

    @Test
    void testSubmitResult_classicMode() {
        // 工厂和策略 Mock
        when(strategyFactory.get(GameMode.CLASSIC)).thenReturn(gameStrategy);
        when(gameStrategy.calculateScore(1, 2)).thenReturn(50);
        when(gameStrategy.evaluateLevel(1)).thenReturn(FanLevel.PASSERBY);
        when(gameStrategy.getPostSubmitHooks()).thenReturn(List.of());

        // Mapper Mock
        when(gameRecordMapper.selectOne(any())).thenReturn(null);
        when(gameRecordMapper.insert(any(com.jaymetest.model.entity.GameRecord.class))).thenReturn(1);
        when(gameRecordMapper.countTotal()).thenReturn(100L);
        when(gameRecordMapper.countByCorrectCountLessThan(eq(1))).thenReturn(65L);
        GameRoundCache round = new GameRoundCache(GameMode.CLASSIC, null, java.util.Map.of(1L, "A", 2L, "B"));
        round.recordAnswer(1L, true);
        round.recordAnswer(2L, false);
        when(roundCacheManager.getOrThrow("test-uuid")).thenReturn(round);

        GameSubmitRequest request = new GameSubmitRequest();
        request.setRoundId("test-uuid");
        request.setTimeSpentSecs(120);

        GameResultDTO result = statsService.submitResult(request);

        assertNotNull(result);
        assertEquals(50, result.getScore());
        assertEquals(1, result.getCorrectCount());
        assertEquals(2, result.getTotalQuestions());
        assertEquals(FanLevel.PASSERBY.name(), result.getLevel());
        assertEquals(FanLevel.PASSERBY.getTitle(), result.getLevelTitle());
        assertEquals("test-uuid", result.getRoundId());
        assertEquals(GameMode.CLASSIC, result.getMode());
        assertNull(result.getAlbumKey());
        assertFalse(result.isUsedRevival());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void submitAbyssResultKeepsGuestRecordButNormalizesRevival() {
        when(strategyFactory.get(GameMode.ABYSS)).thenReturn(gameStrategy);
        when(gameStrategy.calculateScore(1, 2)).thenReturn(1);
        when(gameStrategy.evaluateLevel(1)).thenReturn(AbyssLevel.ABYSS_TOURIST);
        when(gameStrategy.getPostSubmitHooks()).thenReturn(List.of());
        when(gameRecordMapper.selectOne(any())).thenReturn(null);
        when(gameRecordMapper.insert(any(GameRecord.class))).thenReturn(1);
        when(gameRecordMapper.countTotal()).thenReturn(101L);
        when(gameRecordMapper.countByCorrectCountLessThan(1)).thenReturn(90L);
        GameRoundCache round = new GameRoundCache(true);
        round.addQuestion(1L, "A");
        round.addQuestion(2L, "B");
        round.recordAnswer(1L, true);
        round.recordAnswer(2L, false);
        when(roundCacheManager.getOrThrow("abyss-round")).thenReturn(round);

        GameSubmitRequest request = new GameSubmitRequest();
        request.setRoundId("abyss-round");
        request.setTimeSpentSecs(180);

        GameResultDTO result = statsService.submitResult(request);

        assertEquals("abyss-round", result.getRoundId());
        assertEquals(GameMode.ABYSS, result.getMode());
        assertEquals(1, result.getScore());
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
