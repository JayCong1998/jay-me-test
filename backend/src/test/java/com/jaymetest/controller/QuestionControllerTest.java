package com.jaymetest.controller;

import com.jaymetest.model.dto.AnswerRequest;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.service.QuestionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestionControllerTest {

    @Test
    void answerCheckStaysInTheQuestionControllerEntry() {
        QuestionService questionService = mock(QuestionService.class);
        AnswerResultDTO answer = AnswerResultDTO.builder().correct(true).build();
        when(questionService.checkAnswer("round-1", 1L, "A")).thenReturn(answer);
        QuestionController controller = new QuestionController(questionService);

        AnswerRequest request = new AnswerRequest();
        request.setRoundId("round-1");
        request.setQuestionId(1L);
        request.setSelectedOption("A");
        assertSame(answer, controller.checkAnswer(request).getData());
    }
}
