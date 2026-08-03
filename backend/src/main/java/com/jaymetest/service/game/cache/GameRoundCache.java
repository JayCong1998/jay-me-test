package com.jaymetest.service.game.cache;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.model.enums.GameMode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Round 缓存对象：只保存回合状态，不负责过期策略。
 */
@Getter
public class GameRoundCache {

    private final Map<Long, String> answerMap;
    private final GameMode mode;
    private final String albumKey;

    private int streak = 0;
    private final Set<Long> usedQuestionIds;
    private final Map<Long, Boolean> answerResults = new HashMap<>();
    private int revivalUsed;

    private final List<Long> abyssQuestionOrder = new ArrayList<>();
    private int abyssCurrentQuestionIndex;
    private boolean abyssFailed;

    public GameRoundCache(Map<Long, String> answerMap) {
        this(GameMode.CLASSIC, null, answerMap);
    }

    public GameRoundCache(GameMode mode, String albumKey, Map<Long, String> answerMap) {
        this.answerMap = Map.copyOf(answerMap);
        this.usedQuestionIds = Collections.emptySet();
        this.mode = mode;
        this.albumKey = albumKey;
    }

    public GameRoundCache(boolean abyssMode) {
        if (!abyssMode) {
            throw new IllegalArgumentException("Use the single-arg constructor for non-abyss modes");
        }
        this.answerMap = new HashMap<>();
        this.usedQuestionIds = new HashSet<>();
        this.mode = GameMode.ABYSS;
        this.albumKey = null;
    }

    public synchronized void recordAnswer(Long questionId, boolean correct) {
        if (!answerMap.containsKey(questionId)) {
            throw new BusinessException(404, "题目不存在于该回合中");
        }
        if (answerResults.putIfAbsent(questionId, correct) != null) {
            throw new BusinessException(409, "该题已经作答");
        }
    }

    public synchronized void resetAnswerForRevival(Long questionId) {
        if (!Boolean.FALSE.equals(answerResults.get(questionId))) {
            throw new BusinessException(400, "只有答错的当前题可以复活");
        }
        answerResults.remove(questionId);
        revivalUsed++;
    }

    public synchronized int getCorrectCount() {
        return (int) answerResults.values().stream().filter(Boolean::booleanValue).count();
    }

    public synchronized int getAnsweredCount() {
        return answerResults.size();
    }

    public synchronized int getRevivalUsed() {
        return revivalUsed;
    }

    public synchronized int incrementAndGetStreak() {
        return ++this.streak;
    }

    public synchronized void addQuestion(Long questionId, String correctOption) {
        this.answerMap.put(questionId, correctOption);
        this.usedQuestionIds.add(questionId);
        this.abyssQuestionOrder.add(questionId);
    }

    public synchronized void recordAbyssAnswer(Long questionId, boolean correct) {
        requireCurrentAbyssQuestion(questionId);
        if (abyssFailed) {
            throw new BusinessException(409, "本局深渊已失败，请续命或结算");
        }
        recordAnswer(questionId, correct);
        if (correct) {
            abyssCurrentQuestionIndex++;
        } else {
            abyssFailed = true;
        }
    }

    public synchronized void reviveAbyssAnswer(Long questionId) {
        requireCurrentAbyssQuestion(questionId);
        if (!abyssFailed) {
            throw new BusinessException(400, "当前深渊题未答错，不能续命");
        }
        resetAnswerForRevival(questionId);
        abyssFailed = false;
    }

    public synchronized void requireAbyssFailed() {
        if (mode != GameMode.ABYSS || !abyssFailed) {
            throw new BusinessException(400, "深渊挑战仍在进行，不能提交结果");
        }
    }

    public synchronized void requireAbyssBatchCanBeGenerated() {
        if (mode != GameMode.ABYSS) {
            throw new BusinessException(400, "当前回合不是深渊模式");
        }
        if (abyssFailed) {
            throw new BusinessException(409, "本局深渊已失败，不能获取下一批题目");
        }
        if (abyssCurrentQuestionIndex < abyssQuestionOrder.size()) {
            throw new BusinessException(409, "请完成当前批次后再获取下一批题目");
        }
    }

    private void requireCurrentAbyssQuestion(Long questionId) {
        if (mode != GameMode.ABYSS) {
            throw new BusinessException(400, "当前回合不是深渊模式");
        }
        if (abyssCurrentQuestionIndex >= abyssQuestionOrder.size()) {
            throw new BusinessException(409, "当前批次已完成，请加载下一批题目");
        }
        Long expectedQuestionId = abyssQuestionOrder.get(abyssCurrentQuestionIndex);
        if (!expectedQuestionId.equals(questionId)) {
            throw new BusinessException(409, "请按题目顺序作答");
        }
    }
}
