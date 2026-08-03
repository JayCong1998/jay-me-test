package com.jaymetest.service;

import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.cache.GameRoundCache;
import com.jaymetest.service.game.cache.RoundCacheManager;
import com.jaymetest.service.game.strategy.GameStrategyFactory;
import com.jaymetest.service.game.strategy.impl.AbyssGameStrategy;
import com.jaymetest.service.game.strategy.impl.AlbumGameStrategy;
import com.jaymetest.service.game.strategy.impl.ClassicGameStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private GameStrategyFactory strategyFactory;

    @Mock
    private RoundCacheManager cacheManager;

    @Mock
    private ClassicGameStrategy classicStrategy;

    @Mock
    private AlbumGameStrategy albumStrategy;

    @Mock
    private AbyssGameStrategy abyssStrategy;

    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionService(strategyFactory,
                cacheManager, classicStrategy, albumStrategy, abyssStrategy);
    }

    @Test
    void testStrategyAccessors() {
        assertSame(classicStrategy, questionService.classic());
        assertSame(albumStrategy, questionService.album());
        assertSame(abyssStrategy, questionService.abyss());
        assertSame(cacheManager, questionService.cacheManager());
    }

    @Test
    void checkAnswerDelegatesToTheStrategyOfTheCachedRound() {
        GameRoundCache cache = new GameRoundCache(GameMode.CLASSIC, null, java.util.Map.of(1L, "A"));
        AnswerResultDTO expected = AnswerResultDTO.builder().correct(true).build();
        when(cacheManager.getOrThrow("round-1")).thenReturn(cache);
        when(strategyFactory.get(GameMode.CLASSIC)).thenReturn(classicStrategy);
        when(classicStrategy.checkAnswer("round-1", 1L, "A", cacheManager)).thenReturn(expected);

        AnswerResultDTO result = questionService.checkAnswer("round-1", 1L, "A");

        assertSame(expected, result);
        verify(classicStrategy).checkAnswer("round-1", 1L, "A", cacheManager);
    }
}
