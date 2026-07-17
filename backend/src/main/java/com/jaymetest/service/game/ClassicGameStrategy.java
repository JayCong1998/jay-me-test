package com.jaymetest.service.game;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.QuestionDTO;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.DifficultyLevel;
import com.jaymetest.model.enums.FanLevel;
import com.jaymetest.model.enums.GameMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 经典模式策略：60% 简单 + 40% 中等，打散出题，10 题封顶。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClassicGameStrategy implements GameStrategy {

    private final QuestionMapper questionMapper;

    @Override
    public GameMode getMode() {
        return GameMode.CLASSIC;
    }

    @Override
    public RoundDTO generateRound(int count, String albumKey, RoundCacheManager cacheManager) {
        // 60% 简单 + 40% 中等
        int easyCount = (int) Math.round(count * 0.6);
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

        String roundId = UUID.randomUUID().toString();

        // 构建答案缓存
        Map<Long, String> answerMap = finalQuestions.stream()
                .collect(Collectors.toMap(Question::getId, Question::getCorrectOption));
        cacheManager.put(roundId, new GameRoundCache(answerMap));

        // 组装 DTO
        List<QuestionDTO> questionDTOs = QuestionAssembler.toDTOList(finalQuestions);
        RoundDTO roundDTO = new RoundDTO();
        roundDTO.setRoundId(roundId);
        roundDTO.setQuestions(questionDTOs);

        log.info("经典模式: roundId={}, 题目数={}", roundId, questionDTOs.size());
        return roundDTO;
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
        Question question = questionMapper.selectById(questionId);

        return AnswerResultDTO.builder()
                .correct(correct)
                .correctOption(correctOption)
                .explanation(question != null ? question.getExplanation() : "暂无解析")
                .build();
    }

    @Override
    public boolean supportsRevival() {
        return true;
    }

    @Override
    public String revive(String roundId, Long questionId, RoundCacheManager cacheManager) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);
        String correctOption = cache.getAnswerMap().get(questionId);
        if (correctOption == null) {
            throw new BusinessException(404, "题目不存在于该回合中");
        }
        return correctOption;
    }

    @Override
    public int calculateScore(int correctCount, int totalQuestions) {
        return correctCount * 10;
    }

    @Override
    public LevelInfo evaluateLevel(int correctCount) {
        return FanLevel.fromScore(correctCount);
    }
}
