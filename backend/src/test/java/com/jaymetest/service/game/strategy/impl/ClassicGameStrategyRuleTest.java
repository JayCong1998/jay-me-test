package com.jaymetest.service.game.strategy.impl;

import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.DifficultyLevel;
import com.jaymetest.service.game.strategy.GameStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassicGameStrategyRuleTest {

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private GameStrategy.RoundCacheManager cacheManager;

    @Test
    void usesConfiguredQuestionCountAndDifficultyWeights() {
        ClassicGameProperties rules = new ClassicGameProperties();
        rules.setQuestionCount(5);
        rules.setEasyWeight(0.4);
        when(questionMapper.selectRandomByDifficulty(eq(DifficultyLevel.EASY.name()), anyInt()))
                .thenReturn(List.of(question(1), question(2)));
        when(questionMapper.selectRandomByDifficulty(eq(DifficultyLevel.MEDIUM.name()), anyInt()))
                .thenReturn(List.of(question(3), question(4), question(5)));

        ClassicGameStrategy strategy = new ClassicGameStrategy(questionMapper, rules);

        assertEquals(5, strategy.generateRound(null, cacheManager).getQuestions().size());
    }

    private Question question(long id) {
        Question question = new Question();
        question.setId(id);
        question.setCorrectOption("A");
        question.setOptionA("A");
        question.setOptionB("B");
        question.setOptionC("C");
        question.setOptionD("D");
        return question;
    }
}
