package com.jaymetest.service.game.strategy.impl;

import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.DifficultyLevel;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.level.LevelEvaluator;
import com.jaymetest.service.game.level.LevelInfo;
import com.jaymetest.service.game.strategy.AbstractFixedRoundGameStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 经典模式策略：只保留经典模式的抽题和等级规则，固定局流程由父类统一编排。
 */
@Slf4j
@Component
public class ClassicGameStrategy extends AbstractFixedRoundGameStrategy {

    private final ClassicGameProperties gameRules;

    public ClassicGameStrategy(QuestionMapper questionMapper, ClassicGameProperties gameRules) {
        super(questionMapper);
        this.gameRules = gameRules;
    }

    @Override
    public GameMode getMode() {
        return GameMode.CLASSIC;
    }

    @Override
    protected List<Question> selectQuestions(String albumKey) {
        int count = gameRules.getQuestionCount();
        int easyCount = (int) Math.round(count * gameRules.getEasyWeight());
        int mediumCount = count - easyCount;

        List<Question> easyQuestions = questionMapper.selectRandomByDifficulty(
                DifficultyLevel.EASY.name(), easyCount);
        List<Question> mediumQuestions = questionMapper.selectRandomByDifficulty(
                DifficultyLevel.MEDIUM.name(), mediumCount);

        List<Question> allQuestions = new ArrayList<>(easyQuestions);
        allQuestions.addAll(mediumQuestions);
        Collections.shuffle(allQuestions);

        List<Question> finalQuestions = allQuestions.subList(0,
                Math.min(allQuestions.size(), count));
        log.info("经典模式: 题目数={}", finalQuestions.size());
        return finalQuestions;
    }

    @Override
    public LevelInfo evaluateLevel(int correctCount) {
        return LevelEvaluator.evaluate(fixedScorePercent(correctCount, gameRules.getQuestionCount()), gameRules.getLevels());
    }
}
