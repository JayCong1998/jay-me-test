package com.jaymetest.service.game.strategy.impl;

import com.jaymetest.config.AlbumGameProperties;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.hook.AlbumUnlockHook;
import com.jaymetest.service.game.hook.PostSubmitHook;
import com.jaymetest.service.game.level.LevelEvaluator;
import com.jaymetest.service.game.level.LevelInfo;
import com.jaymetest.service.game.strategy.AbstractFixedRoundGameStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 专辑闯关策略：只保留专辑抽题、登录要求、等级规则和提交后解锁钩子。
 */
@Slf4j
@Component
public class AlbumGameStrategy extends AbstractFixedRoundGameStrategy {

    private final AlbumUnlockHook albumUnlockHook;
    private final AlbumGameProperties gameRules;

    public AlbumGameStrategy(QuestionMapper questionMapper, AlbumUnlockHook albumUnlockHook, AlbumGameProperties gameRules) {
        super(questionMapper);
        this.albumUnlockHook = albumUnlockHook;
        this.gameRules = gameRules;
    }

    @Override
    public GameMode getMode() {
        return GameMode.ALBUM;
    }

    @Override
    public boolean requiresAuth() {
        return true;
    }

    @Override
    protected List<Question> selectQuestions(String albumKey) {
        int count = gameRules.getQuestionCount();
        if (albumKey == null || albumKey.isEmpty()) {
            throw new BusinessException(400, "专辑模式缺少 albumKey 参数");
        }

        List<Question> allQuestions = questionMapper.selectRandomByAlbum(albumKey, count);
        Collections.shuffle(allQuestions);

        List<Question> finalQuestions = allQuestions.subList(0,
                Math.min(allQuestions.size(), count));
        log.info("专辑模式: album={}, 题目数={}", albumKey, finalQuestions.size());
        return finalQuestions;
    }

    @Override
    public LevelInfo evaluateLevel(int correctCount) {
        return LevelEvaluator.evaluate(fixedScorePercent(correctCount, gameRules.getQuestionCount()), gameRules.getLevels());
    }

    @Override
    public List<PostSubmitHook> getPostSubmitHooks() {
        return List.of(albumUnlockHook);
    }
}
