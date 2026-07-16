package com.jaymetest.service;

import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.model.entity.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionService questionService;

    private List<Question> mockQuestions;

    @BeforeEach
    void setUp() {
        mockQuestions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Question q = new Question();
            q.setId((long) i);
            q.setCategory(i <= 6 ? "LYRICS" : "ALBUM");
            q.setDifficulty(i <= 6 ? "EASY" : "MEDIUM");
            q.setQuestionText("Test question " + i);
            q.setOptionA("Option A");
            q.setOptionB("Option B");
            q.setOptionC("Option C");
            q.setOptionD("Option D");
            q.setCorrectOption(i % 2 == 0 ? "A" : "B");
            q.setExplanation("Explanation " + i);
            mockQuestions.add(q);
        }
    }

    @Test
    void testGenerateRound() {
        when(questionMapper.selectRandomByDifficulty(eq("EASY"), anyInt()))
                .thenReturn(mockQuestions.subList(0, 6));
        when(questionMapper.selectRandomByDifficulty(eq("MEDIUM"), anyInt()))
                .thenReturn(mockQuestions.subList(6, 10));

        RoundDTO result = questionService.generateRound(10);

        assertNotNull(result);
        assertNotNull(result.getRoundId());
        assertFalse(result.getRoundId().isEmpty());
        assertNotNull(result.getQuestions());
    }
}
