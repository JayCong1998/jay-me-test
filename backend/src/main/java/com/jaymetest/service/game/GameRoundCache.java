package com.jaymetest.service.game;

import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Round 缓存对象 — 纯数据载体，不含模式专有逻辑。
 * <p>
 * 经典/专辑模式：构造时传入完整 answerMap（通过 {@link #GameRoundCache(Map)}），
 * 一次性写入，后续只读。
 * </p>
 * <p>
 * 无尽深渊模式：使用无参构造 + {@link #addQuestion} 逐步追加，
 * streak 和 usedQuestionIds 仅在深渊模式下有意义。
 * </p>
 */
@Getter
public class GameRoundCache {

    /** questionId → correctOption */
    private final Map<Long, String> answerMap;
    private final Instant createdAt;

    /** 当前连续答对题数（深渊模式用） */
    private int streak = 0;

    /** 已出现的题目 ID 集合（深渊模式去重用） */
    private final Set<Long> usedQuestionIds;

    // ---- 经典/专辑模式构造器 ----

    public GameRoundCache(Map<Long, String> answerMap) {
        this.answerMap = Map.copyOf(answerMap);  // 不可变，安全共享
        this.createdAt = Instant.now();
        this.usedQuestionIds = Collections.emptySet(); // 不可变空集合
    }

    // ---- 深渊模式构造器 ----

    public GameRoundCache(boolean abyssMode) {
        if (!abyssMode) {
            throw new IllegalArgumentException("Use the single-arg constructor for non-abyss modes");
        }
        this.answerMap = new HashMap<>();
        this.createdAt = Instant.now();
        this.usedQuestionIds = new HashSet<>();
    }

    // ---- 通用方法 ----

    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plusSeconds(30 * 60)); // 30 分钟过期
    }

    // ---- 深渊模式专有方法 ----

    /** 递增 streak 并返回新值 */
    public int incrementAndGetStreak() {
        return ++this.streak;
    }

    /** 追加一道题目到缓存（深渊模式逐题添加） */
    public void addQuestion(Long questionId, String correctOption) {
        this.answerMap.put(questionId, correctOption);
        this.usedQuestionIds.add(questionId);
    }
}
