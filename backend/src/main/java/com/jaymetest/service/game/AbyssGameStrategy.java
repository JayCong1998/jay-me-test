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
    private final AbyssDifficultyPolicy difficultyPolicy;

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
            DifficultySelection selection = difficultyPolicy.select(startStreak + i);
            Question q = selectRandomExcluding(selection, cache.getUsedQuestionIds());
            if (q == null && selection != DifficultySelection.ANY) {
                q = selectRandomExcluding(DifficultySelection.ANY, cache.getUsedQuestionIds());
            }
            if (q == null) {
                throw new BusinessException(409, "深渊模式题库不足，请补充题目后重试");
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

    // ---- 内部工具方法 ----

    private Question selectRandomExcluding(DifficultySelection selection, Set<Long> excludeIds) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (selection != DifficultySelection.ANY) {
            wrapper.eq(Question::getDifficulty, selection.name());
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
