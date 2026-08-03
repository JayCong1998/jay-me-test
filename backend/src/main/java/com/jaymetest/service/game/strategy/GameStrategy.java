package com.jaymetest.service.game.strategy;

import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.cache.GameRoundCache;
import com.jaymetest.service.game.hook.PostSubmitHook;
import com.jaymetest.service.game.level.LevelInfo;

import java.util.List;

/**
 * 游戏模式公共策略接口，只定义所有玩法共享的校验、计分、等级和提交后处理能力。
 */
public interface GameStrategy {

    GameMode getMode();

    default boolean requiresAuth() {
        return false;
    }

    AnswerResultDTO checkAnswer(String roundId, Long questionId, String selectedOption,
                                RoundCacheManager roundCacheManager);

    default String revive(String roundId, Long questionId, RoundCacheManager roundCacheManager) {
        throw new UnsupportedOperationException("当前模式不支持复活");
    }

    int calculateScore(int correctCount, int totalQuestions);

    LevelInfo evaluateLevel(int correctCount);

    default List<PostSubmitHook> getPostSubmitHooks() {
        return List.of();
    }

    interface RoundCacheManager {
        void put(String roundId, GameRoundCache cache);

        GameRoundCache get(String roundId);

        void remove(String roundId);

        GameRoundCache getOrThrow(String roundId);
    }
}
