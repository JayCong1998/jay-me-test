package com.jaymetest.service.game.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaymetest.config.AbyssGameProperties;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AbyssStepDTO;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.QuestionDTO;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.abyss.AbyssDifficultyPolicy;
import com.jaymetest.service.game.abyss.DifficultySelection;
import com.jaymetest.service.game.cache.GameRoundCache;
import com.jaymetest.service.game.cache.RoundCacheManager;
import com.jaymetest.service.game.level.LevelEvaluator;
import com.jaymetest.service.game.level.LevelInfo;
import com.jaymetest.service.game.strategy.BatchRoundStrategy;
import com.jaymetest.service.game.support.QuestionAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 无尽深渊模式策略：批次式生成题目，按服务端顺序校验，答错后可按规则复活。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbyssGameStrategy implements BatchRoundStrategy {

    private final QuestionMapper questionMapper;
    private final AbyssDifficultyPolicy difficultyPolicy;
    private final AbyssGameProperties gameRules;

    @Override
    public GameMode getMode() {
        return GameMode.ABYSS;
    }

    @Override
    public boolean requiresAuth() {
        return true;
    }

    @Override
    public AbyssStepDTO generateStart(RoundCacheManager cacheManager) {
        String roundId = UUID.randomUUID().toString();
        GameRoundCache cache = new GameRoundCache(true);

        List<Question> questions = generateQuestions(0, cache);
        cacheManager.put(roundId, cache);

        log.info("深渊模式开始: roundId={}, 首批题目数={}", roundId, questions.size());
        return buildStepDTO(roundId, questions, 0, cache);
    }

    @Override
    public AbyssStepDTO generateBatch(String roundId, RoundCacheManager cacheManager) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);
        cache.requireAbyssBatchCanBeGenerated();
        int currentStreak = cache.getStreak();

        List<Question> questions = generateQuestions(currentStreak, cache);

        log.info("深渊模式下一批: roundId={}, 当前streak={}, 新增题目数={}",
                roundId, currentStreak, questions.size());
        return buildStepDTO(roundId, questions, currentStreak, cache);
    }

    private List<Question> generateQuestions(int startStreak, GameRoundCache cache) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < gameRules.getBatchSize(); i++) {
            DifficultySelection selection = difficultyPolicy.select(startStreak + i);
            Question question = selectRandomExcluding(selection, cache.getUsedQuestionIds());
            if (question == null && selection != DifficultySelection.ANY) {
                question = selectRandomExcluding(DifficultySelection.ANY, cache.getUsedQuestionIds());
            }
            if (question == null) {
                throw new BusinessException(409, "深渊模式题库不足，请补充题目后重试");
            }
            cache.addQuestion(question.getId(), question.getCorrectOption());
            questions.add(question);
        }
        return questions;
    }

    @Override
    public AnswerResultDTO checkAnswer(String roundId, Long questionId, String selectedOption,
                                       RoundCacheManager cacheManager) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);

        String correctOption = cache.getAnswerMap().get(questionId);
        if (correctOption == null) {
            throw new BusinessException(404, "题目不存在于该回合中");
        }

        boolean correct = correctOption.equalsIgnoreCase(selectedOption.trim().toUpperCase());
        cache.recordAbyssAnswer(questionId, correct);
        Question question = questionMapper.selectById(questionId);

        if (correct) {
            cache.incrementAndGetStreak();
        }

        boolean canRevive = !correct && canRevive(cache);
        return AnswerResultDTO.builder()
                .correct(correct)
                .correctOption(canRevive ? null : correctOption)
                .explanation(canRevive ? null : question != null ? question.getExplanation() : "暂无解析")
                .canRevive(canRevive)
                .build();
    }

    @Override
    public String revive(String roundId, Long questionId, RoundCacheManager cacheManager) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);
        if (!canRevive(cache)) {
            throw new BusinessException(409, "本局深渊续命机会已用完");
        }
        if (!cache.getAnswerMap().containsKey(questionId)) {
            throw new BusinessException(404, "题目不属于该回合");
        }
        cache.reviveAbyssAnswer(questionId);
        return cache.getAnswerMap().get(questionId);
    }

    public int getRemainingRevivals(String roundId, RoundCacheManager cacheManager) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);
        return Math.max(0, gameRules.getRevivalCount() - cache.getRevivalUsed());
    }

    @Override
    public int calculateScore(int correctCount, int totalQuestions) {
        return correctCount;
    }

    @Override
    public LevelInfo evaluateLevel(int correctCount) {
        return LevelEvaluator.evaluate(correctCount, gameRules.getLevels());
    }

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
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private boolean canRevive(GameRoundCache cache) {
        return cache.getRevivalUsed() < gameRules.getRevivalCount();
    }

    private AbyssStepDTO buildStepDTO(String roundId, List<Question> questions, int streak, GameRoundCache cache) {
        List<QuestionDTO> questionDTOs = QuestionAssembler.toDTOList(questions);
        AbyssStepDTO result = new AbyssStepDTO();
        result.setRoundId(roundId);
        result.setQuestions(questionDTOs);
        result.setStreak(streak);
        result.setRevivalRemaining(Math.max(0, gameRules.getRevivalCount() - cache.getRevivalUsed()));
        return result;
    }
}
