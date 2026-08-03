package com.jaymetest.controller;

import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.service.QuestionService;
import com.jaymetest.service.game.strategy.impl.ClassicGameStrategy;
import com.jaymetest.service.game.cache.RoundCacheManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassicControllerTest {

    @Test
    void classicRoundStaysInTheClassicControllerEntry() {
        QuestionService questionService = mock(QuestionService.class);
        ClassicGameStrategy classicStrategy = mock(ClassicGameStrategy.class);
        RoundCacheManager cacheManager = mock(RoundCacheManager.class);
        RoundDTO round = new RoundDTO();
        when(questionService.classic()).thenReturn(classicStrategy);
        when(questionService.cacheManager()).thenReturn(cacheManager);
        when(classicStrategy.generateRound(null, cacheManager)).thenReturn(round);
        ClassicController controller = new ClassicController(questionService);

        assertSame(round, controller.getRound().getData());
        verify(classicStrategy).generateRound(null, cacheManager);
    }
}
