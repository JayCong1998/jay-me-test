package com.jaymetest.controller;

import com.jaymetest.model.dto.AbyssBatchRequest;
import com.jaymetest.model.dto.AbyssStepDTO;
import com.jaymetest.model.dto.AnswerRequest;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.ReviveRequest;
import com.jaymetest.service.QuestionService;
import com.jaymetest.service.game.strategy.impl.AbyssGameStrategy;
import com.jaymetest.service.game.cache.RoundCacheManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbyssControllerTest {

    @Test
    void abyssLifecycleStaysInTheAbyssControllerEntry() {
        QuestionService questionService = mock(QuestionService.class);
        AbyssGameStrategy abyssStrategy = mock(AbyssGameStrategy.class);
        RoundCacheManager cacheManager = mock(RoundCacheManager.class);
        AbyssStepDTO start = new AbyssStepDTO();
        AbyssStepDTO batch = new AbyssStepDTO();
        AnswerResultDTO answer = AnswerResultDTO.builder().correct(true).build();
        when(questionService.abyss()).thenReturn(abyssStrategy);
        when(questionService.cacheManager()).thenReturn(cacheManager);
        when(abyssStrategy.generateStart(cacheManager)).thenReturn(start);
        when(abyssStrategy.generateBatch("round-1", cacheManager)).thenReturn(batch);
        when(abyssStrategy.checkAnswer("round-1", 1L, "A", cacheManager)).thenReturn(answer);
        when(abyssStrategy.getRemainingRevivals("round-1", cacheManager)).thenReturn(0);
        AbyssController controller = new AbyssController(questionService);

        assertSame(start, controller.startAbyss().getData());

        AbyssBatchRequest batchRequest = new AbyssBatchRequest();
        batchRequest.setRoundId("round-1");
        assertSame(batch, controller.nextBatch(batchRequest).getData());

        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setRoundId("round-1");
        answerRequest.setQuestionId(1L);
        answerRequest.setSelectedOption("A");
        assertSame(answer, controller.checkAnswer(answerRequest).getData());

        ReviveRequest reviveRequest = new ReviveRequest();
        reviveRequest.setRoundId("round-1");
        reviveRequest.setQuestionId(1L);
        assertEquals(0, controller.revive(reviveRequest).getData().get("remainingRevivals"));
        verify(abyssStrategy).revive("round-1", 1L, cacheManager);
    }
}
