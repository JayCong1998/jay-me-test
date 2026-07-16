package com.jaymetest.service;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.*;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.DifficultyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 题目服务：抽题 + 校验 + 复活
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;

    /** Round 缓存: roundId → RoundCache */
    private final ConcurrentHashMap<String, RoundCache> roundCache = new ConcurrentHashMap<>();

    /**
     * 随机抽取一局题目
     */
    public RoundDTO generateRound(int count) {
        int easyCount = (int) Math.round(count * 0.6);
        int mediumCount = count - easyCount;

        List<Question> easyQuestions = questionMapper.selectRandomByDifficulty(
                DifficultyLevel.EASY.name(), easyCount);
        List<Question> mediumQuestions = questionMapper.selectRandomByDifficulty(
                DifficultyLevel.MEDIUM.name(), mediumCount);

        List<Question> allQuestions = new ArrayList<>(easyQuestions);
        allQuestions.addAll(mediumQuestions);
        Collections.shuffle(allQuestions);

        // 实际能抽取的题目数量可能少于请求数量
        List<Question> finalQuestions = allQuestions.subList(0,
                Math.min(allQuestions.size(), count));

        String roundId = UUID.randomUUID().toString();

        // 构建答案缓存
        Map<Long, String> answerMap = finalQuestions.stream()
                .collect(Collectors.toMap(Question::getId, Question::getCorrectOption));
        roundCache.put(roundId, new RoundCache(answerMap));

        // 转换为 DTO
        List<QuestionDTO> questionDTOs = finalQuestions.stream().map(q -> {
            QuestionDTO dto = new QuestionDTO();
            dto.setId(q.getId());
            dto.setCategory(q.getCategory());
            dto.setDifficulty(q.getDifficulty());
            dto.setQuestionText(q.getQuestionText());
            dto.setOptions(q.getOptionsAsList());
            return dto;
        }).collect(Collectors.toList());

        RoundDTO roundDTO = new RoundDTO();
        roundDTO.setRoundId(roundId);
        roundDTO.setQuestions(questionDTOs);

        log.info("生成新回合 roundId={}, 题目数={}", roundId, questionDTOs.size());
        return roundDTO;
    }

    /**
     * 校验单题答案
     */
    public AnswerResultDTO checkAnswer(String roundId, Long questionId, String selectedOption) {
        RoundCache cache = roundCache.get(roundId);
        if (cache == null) {
            throw new BusinessException(404, "回合不存在或已过期");
        }
        if (cache.isExpired()) {
            roundCache.remove(roundId);
            throw new BusinessException(404, "回合已过期（超过30分钟），请重新开始");
        }

        String correctOption = cache.getAnswerMap().get(questionId);
        if (correctOption == null) {
            throw new BusinessException(404, "题目不存在于该回合中");
        }

        boolean correct = correctOption.equalsIgnoreCase(selectedOption.trim().toUpperCase());

        // 从数据库获取解析
        Question question = questionMapper.selectById(questionId);

        return AnswerResultDTO.builder()
                .correct(correct)
                .correctOption(correctOption)
                .explanation(question != null ? question.getExplanation() : "暂无解析")
                .build();
    }

    /**
     * 获取正确答案（复活用，不记录为一次新的答题）
     */
    public String getCorrectAnswer(String roundId, Long questionId) {
        RoundCache cache = roundCache.get(roundId);
        if (cache == null || cache.isExpired()) {
            throw new BusinessException(404, "回合不存在或已过期");
        }
        String correctOption = cache.getAnswerMap().get(questionId);
        if (correctOption == null) {
            throw new BusinessException(404, "题目不存在于该回合中");
        }
        return correctOption;
    }

    /**
     * 定时清理过期缓存
     */
    @Scheduled(fixedRate = 600_000) // 每 10 分钟
    public void cleanExpiredCache() {
        int before = roundCache.size();
        roundCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int after = roundCache.size();
        if (before != after) {
            log.info("清理过期 Round 缓存: {} → {}", before, after);
        }
    }
}
