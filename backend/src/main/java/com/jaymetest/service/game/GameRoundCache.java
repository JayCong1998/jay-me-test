package com.jaymetest.service.game;

import lombok.Getter;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.model.enums.GameMode;

import java.time.Instant;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private final GameMode mode;
    private final String albumKey;

    /** 当前连续答对题数（深渊模式用） */
    private int streak = 0;

    /** 已出现的题目 ID 集合（深渊模式去重用） */
    private final Set<Long> usedQuestionIds;
    private final Map<Long, Boolean> answerResults = new HashMap<>();
    private int revivalUsed;

    /** 深渊模式按服务端下发顺序作答，避免客户端跳题伪造 streak。 */
    private final List<Long> abyssQuestionOrder = new ArrayList<>();
    private int abyssCurrentQuestionIndex;
    private boolean abyssFailed;

    // ---- 经典/专辑模式构造器 ----

    public GameRoundCache(Map<Long, String> answerMap) {
        this(GameMode.CLASSIC, null, answerMap);
    }

    public GameRoundCache(GameMode mode, String albumKey, Map<Long, String> answerMap) {
        this.answerMap = Map.copyOf(answerMap);  // 不可变，安全共享
        this.createdAt = Instant.now();
        this.usedQuestionIds = Collections.emptySet(); // 不可变空集合
        this.mode = mode;
        this.albumKey = albumKey;
    }

    // ---- 深渊模式构造器 ----

    public GameRoundCache(boolean abyssMode) {
        if (!abyssMode) {
            throw new IllegalArgumentException("Use the single-arg constructor for non-abyss modes");
        }
        this.answerMap = new HashMap<>();
        this.createdAt = Instant.now();
        this.usedQuestionIds = new HashSet<>();
        this.mode = GameMode.ABYSS;
        this.albumKey = null;
    }

    // ---- 通用方法 ----

    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plusSeconds(30 * 60)); // 30 分钟过期
    }

    /** 记录服务端校验过的作答，结算必须以此为准。 */
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

    // ---- 深渊模式专有方法 ----

    /** 递增 streak 并返回新值 */
    public synchronized int incrementAndGetStreak() {
        return ++this.streak;
    }

    /** 追加一道题目到缓存（深渊模式逐题添加） */
    public synchronized void addQuestion(Long questionId, String correctOption) {
        this.answerMap.put(questionId, correctOption);
        this.usedQuestionIds.add(questionId);
        this.abyssQuestionOrder.add(questionId);
    }

    /** 仅允许作答服务端指定的当前深渊题；答错后 Round 进入失败态。 */
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

    /** 续命只可清除当前错误题，并恢复到可继续作答的状态。 */
    public synchronized void reviveAbyssAnswer(Long questionId) {
        requireCurrentAbyssQuestion(questionId);
        if (!abyssFailed) {
            throw new BusinessException(400, "当前深渊题未答错，不能续命");
        }
        resetAnswerForRevival(questionId);
        abyssFailed = false;
    }

    /** 深渊成绩只能在本局失败后结算，不能提前提交存活中的局。 */
    public synchronized void requireAbyssFailed() {
        if (mode != GameMode.ABYSS || !abyssFailed) {
            throw new BusinessException(400, "深渊挑战仍在进行，不能提交结果");
        }
    }

    /** 下一批难度必须基于完成当前批次后的 streak，不能在中途预取。 */
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
