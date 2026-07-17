package com.jaymetest.service.game;

import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.model.enums.DifficultyLevel;

import java.util.List;

/**
 * 游戏模式策略接口 — 每种游戏模式实现此接口，封装该模式的全部差异化行为。
 *
 * <p>新增游戏模式时：实现此接口 → Spring 自动注册到 {@link GameStrategyFactory} → 完成。</p>
 */
public interface GameStrategy {

    /** 该策略对应的游戏模式 */
    GameMode getMode();

    /** 是否需要登录才能参与 */
    default boolean requiresAuth() {
        return false;
    }

    /**
     * 生成一局题目并缓存答案
     * @param count      题目数量
     * @param albumKey   专辑标识（仅 ALBUM 模式使用，其他模式传 null）
     * @param roundCacheManager 缓存管理器，策略通过它将答案写入缓存
     * @return 不含正确答案的题目列表
     */
    RoundDTO generateRound(int count, String albumKey, RoundCacheManager roundCacheManager);

    /**
     * 校验单题答案
     * @return 校验结果（是否答对 + 正确答案 + 解析）
     */
    AnswerResultDTO checkAnswer(String roundId, Long questionId, String selectedOption,
                                RoundCacheManager roundCacheManager);

    /** 是否支持复活 */
    default boolean supportsRevival() {
        return false;
    }

    /** 复活：返回正确答案，不做任何副作用 */
    default String revive(String roundId, Long questionId, RoundCacheManager roundCacheManager) {
        throw new UnsupportedOperationException("当前模式不支持复活");
    }

    /** 计分公式 */
    int calculateScore(int correctCount, int totalQuestions);

    /** 等级评定（返回 LevelInfo 统一接口） */
    LevelInfo evaluateLevel(int correctCount);

    /** 提交后置处理钩子（如专辑解锁、成就判定等） */
    default List<PostSubmitHook> getPostSubmitHooks() {
        return List.of();
    }

    // ============================================================
    // 缓存管理接口 — 策略通过此接口操作缓存，不直接持有 ConcurrentHashMap
    // ============================================================

    interface RoundCacheManager {
        /** 向 roundId 写入完整答案映射（经典/专辑模式 — 一次性写入） */
        void put(String roundId, GameRoundCache cache);

        /** 获取缓存 */
        GameRoundCache get(String roundId);

        /** 删除缓存 */
        void remove(String roundId);

        /** 检查 round 是否有效（存在且未过期），否则抛 BusinessException */
        GameRoundCache getOrThrow(String roundId);
    }
}
