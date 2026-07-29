package com.jaymetest.service.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaymetest.config.GameRuleProperties;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AbyssStepDTO;
import com.jaymetest.model.entity.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbyssGameStrategyTest {

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private AbyssDifficultyPolicy difficultyPolicy;

    @Mock
    private GameStrategy.RoundCacheManager cacheManager;

    private AbyssGameStrategy strategy;
    private AtomicLong questionIds;

    @BeforeEach
    void setUp() {
        strategy = new AbyssGameStrategy(questionMapper, difficultyPolicy, new GameRuleProperties());
        questionIds = new AtomicLong();
    }

    @Test
    void consumesPolicyForEveryGeneratedQuestion() {
        when(difficultyPolicy.select(any(Integer.class))).thenReturn(DifficultySelection.HARD);
        when(questionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> List.of(question(questionIds.incrementAndGet())));

        AbyssStepDTO result = strategy.generateStart(cacheManager);

        assertEquals(5, result.getQuestions().size());
        for (int streak = 0; streak < 5; streak++) {
            verify(difficultyPolicy).select(streak);
        }
        ArgumentCaptor<LambdaQueryWrapper<Question>> captor = wrapperCaptor();
        verify(questionMapper, times(5)).selectList(captor.capture());
        assertFalse(captor.getAllValues().getFirst().getExpression().getNormal().isEmpty());
    }

    @Test
    void anySelectionDoesNotAddDifficultyCondition() {
        when(difficultyPolicy.select(any(Integer.class))).thenReturn(DifficultySelection.ANY);
        when(questionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> List.of(question(questionIds.incrementAndGet())));

        strategy.generateStart(cacheManager);

        ArgumentCaptor<LambdaQueryWrapper<Question>> captor = wrapperCaptor();
        verify(questionMapper, times(5)).selectList(captor.capture());
        assertTrue(captor.getAllValues().getFirst().getExpression().getNormal().isEmpty());
    }

    @Test
    void fallsBackToAnyWhenSelectedDifficultyHasNoQuestions() {
        when(difficultyPolicy.select(any(Integer.class))).thenReturn(DifficultySelection.HARD);
        AtomicLong queryCount = new AtomicLong();
        when(questionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> queryCount.incrementAndGet() % 2 == 1
                        ? List.of()
                        : List.of(question(questionIds.incrementAndGet())));

        AbyssStepDTO result = strategy.generateStart(cacheManager);

        assertEquals(5, result.getQuestions().size());
        ArgumentCaptor<LambdaQueryWrapper<Question>> captor = wrapperCaptor();
        verify(questionMapper, times(10)).selectList(captor.capture());
        assertFalse(captor.getAllValues().get(0).getExpression().getNormal().isEmpty());
        assertTrue(captor.getAllValues().get(1).getExpression().getNormal().isEmpty());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<LambdaQueryWrapper<Question>> wrapperCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(LambdaQueryWrapper.class);
    }

    private Question question(long id) {
        Question question = new Question();
        question.setId(id);
        question.setCategory("LYRICS");
        question.setDifficulty("HARD");
        question.setQuestionText("测试题 " + id);
        question.setOptionA("A");
        question.setOptionB("B");
        question.setOptionC("C");
        question.setOptionD("D");
        question.setCorrectOption("A");
        return question;
    }
}
