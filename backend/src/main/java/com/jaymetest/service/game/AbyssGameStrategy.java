package com.jaymetest.service.game;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AbyssStepDTO;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.QuestionDTO;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.AbyssLevel;
import com.jaymetest.model.enums.DifficultyLevel;
import com.jaymetest.model.enums.GameMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 无尽深渊模式策略：无限连答，难度递增，每批 5 题，逐题生成。
 *
 * <p>难度阶梯配置见 {@code application.yml} 中的 {@code game.abyss.difficulty}。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbyssGameStrategy implements GameStrategy {

    private static final int BATCH_SIZE = 5;

    private final QuestionMapper questionMapper;

    @Override
    public GameMode getMode() {
        return GameMode.ABYSS;
    }

    @Override
    public boolean requiresAuth() {
        return true;
    }

    // ---- 经典/专辑模式接口（深渊不使用 generateRound，走自定义方法） ----

    @Override
    public RoundDTO generateRound(int count, String albumKey, RoundCacheManager cacheManager) {
        throw new UnsupportedOperationException("深渊模式请使用 generateStart / generateBatch");
    }

    // ---- 深渊专有方法 ----

    /** 开始深渊挑战，返回首批题目 */
    public AbyssStepDTO generateStart(RoundCacheManager cacheManager) {
        String roundId = UUID.randomUUID().toString();
        GameRoundCache cache = new GameRoundCache(true); // abyssMode

        List<Question> questions = generateQuestions(0, cache);
        cacheManager.put(roundId, cache);

        log.info("深渊模式开始 roundId={}, 首批 {} 题", roundId, questions.size());
        return buildStepDTO(roundId, questions, 0);
    }

    /** 获取下一批题目（前端在倒数第 2 题时静默预加载） */
    public AbyssStepDTO generateBatch(String roundId, RoundCacheManager cacheManager) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);
        int currentStreak = cache.getStreak();

        List<Question> questions = generateQuestions(currentStreak, cache);

        log.info("深渊模式下一批 roundId={}, 当前streak={}, 新增 {} 题",
                roundId, currentStreak, questions.size());
        return buildStepDTO(roundId, questions, currentStreak);
    }

    private List<Question> generateQuestions(int startStreak, GameRoundCache cache) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            String difficulty = determineDifficulty(startStreak + i);
            Question q = selectRandomExcluding(difficulty, cache.getUsedQuestionIds());
            if (q == null) {
                q = selectRandomExcluding(null, cache.getUsedQuestionIds());
            }
            if (q == null) {
                log.warn("深渊模式题库耗尽，已出题 {} 道", cache.getUsedQuestionIds().size());
                break;
            }
            cache.addQuestion(q.getId(), q.getCorrectOption());
            questions.add(q);
        }
        return questions;
    }

    // ---- 答案校验 ----

    @Override
    public AnswerResultDTO checkAnswer(String roundId, Long questionId, String selectedOption,
                                       RoundCacheManager cacheManager) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);

        String correctOption = cache.getAnswerMap().get(questionId);
        if (correctOption == null) {
            throw new BusinessException(404, "题目不存在于该回合中");
        }

        boolean correct = correctOption.equalsIgnoreCase(selectedOption.trim().toUpperCase());
        Question question = questionMapper.selectById(questionId);

        // 答对时自动递增 streak
        if (correct) {
            cache.incrementAndGetStreak();
        }

        return AnswerResultDTO.builder()
                .correct(correct)
                .correctOption(correctOption)
                .explanation(question != null ? question.getExplanation() : "暂无解析")
                .build();
    }

    // ---- 计分 / 等级 ----

    @Override
    public int calculateScore(int correctCount, int totalQuestions) {
        // 深渊模式：分数 = 连续答对数（streak）
        return correctCount;
    }

    @Override
    public LevelInfo evaluateLevel(int correctCount) {
        return AbyssLevel.fromStreak(correctCount);
    }

    // ---- 难度阶梯 ----

    /**
     * 根据 streak 动态决定难度。
     * <p>难度阶梯（可配置化后从 AbyssDifficultyConfig 读取）：
     * <pre>
     *   streak 0-2:   100% EASY
     *   streak 3-5:   70% MEDIUM, 30% EASY
     *   streak 6-9:   60% HARD,  40% MEDIUM
     *   streak 10-14: 70% HARD,  30% MEDIUM
     *   streak 15-19: 100% HARD
     *   streak 20-29: 80% HARD,  20% 不限
     *   streak 30+:   50% HARD,  50% 不限
     * </pre>
     */
    private String determineDifficulty(int streak) {
        if (streak <= 2) return DifficultyLevel.EASY.name();
        if (streak <= 5) return Math.random() < 0.7 ? DifficultyLevel.MEDIUM.name() : DifficultyLevel.EASY.name();
        if (streak <= 9) return Math.random() < 0.6 ? DifficultyLevel.HARD.name() : DifficultyLevel.MEDIUM.name();
        if (streak <= 14) return Math.random() < 0.7 ? DifficultyLevel.HARD.name() : DifficultyLevel.MEDIUM.name();
        if (streak <= 19) return DifficultyLevel.HARD.name();
        if (streak <= 29) return Math.random() < 0.8 ? DifficultyLevel.HARD.name() : null; // null = 不限难度
        return Math.random() < 0.5 ? DifficultyLevel.HARD.name() : null;
    }

    // ---- 内部工具方法 ----

    private Question selectRandomExcluding(String difficulty, Set<Long> excludeIds) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (excludeIds != null && !excludeIds.isEmpty()) {
            wrapper.notIn(Question::getId, excludeIds);
        }
        wrapper.last("ORDER BY RAND() LIMIT 1");
        List<Question> candidates = questionMapper.selectList(wrapper);
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private AbyssStepDTO buildStepDTO(String roundId, List<Question> questions, int streak) {
        List<QuestionDTO> questionDTOs = QuestionAssembler.toDTOList(questions);
        AbyssStepDTO result = new AbyssStepDTO();
        result.setRoundId(roundId);
        result.setQuestions(questionDTOs);
        result.setStreak(streak);
        return result;
    }
}
