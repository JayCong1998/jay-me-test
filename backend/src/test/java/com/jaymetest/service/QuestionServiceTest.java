package com.jaymetest.service;

import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private List<Question> mockQuestions;

    @BeforeEach
    void setUp() {
        questionService = new QuestionService(questionMapper, strategyFactory,
                cacheManager, classicStrategy, albumStrategy, abyssStrategy);

        mockQuestions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Question q = new Question();
            q.setId((long) i);
            q.setCategory(i <= 6 ? "LYRICS" : "WORKS");
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
    void testGenerateRound_classicMode_delegatesToClassicStrategy() {
        RoundDTO expected = new RoundDTO();
        expected.setRoundId("test-round-id");
        when(classicStrategy.generateRound(eq(10), isNull(), any(RoundCacheManager.class)))
                .thenReturn(expected);

        RoundDTO result = questionService.generateRound(10, null);

        assertNotNull(result);
        assertEquals("test-round-id", result.getRoundId());
    }

    @Test
    void testGenerateRound_albumMode_delegatesToAlbumStrategy() {
        RoundDTO expected = new RoundDTO();
        expected.setRoundId("album-round-id");
        when(albumStrategy.generateRound(eq(10), eq("JAY"), any(RoundCacheManager.class)))
                .thenReturn(expected);

        RoundDTO result = questionService.generateRound(10, "JAY");

        assertNotNull(result);
        assertEquals("album-round-id", result.getRoundId());
    }

    @Test
    void testStrategyAccessors() {
        assertSame(classicStrategy, questionService.classic());
        assertSame(albumStrategy, questionService.album());
        assertSame(abyssStrategy, questionService.abyss());
        assertSame(cacheManager, questionService.cacheManager());
        assertSame(strategyFactory, questionService.factory());
    }
}
