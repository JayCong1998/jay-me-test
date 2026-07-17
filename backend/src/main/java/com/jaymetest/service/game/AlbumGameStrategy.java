package com.jaymetest.service.game;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.QuestionDTO;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.FanLevel;
import com.jaymetest.model.enums.GameMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 专辑闯关模式策略：按专辑过滤题目，需登录，通关自动解锁下一专辑。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlbumGameStrategy implements GameStrategy {

    private final QuestionMapper questionMapper;
    private final AlbumUnlockHook albumUnlockHook;

    @Override
    public GameMode getMode() {
        return GameMode.ALBUM;
    }

    @Override
    public boolean requiresAuth() {
        return true;
    }

    @Override
    public RoundDTO generateRound(int count, String albumKey, RoundCacheManager cacheManager) {
        if (albumKey == null || albumKey.isEmpty()) {
            throw new BusinessException(400, "专辑模式缺少 albumKey 参数");
        }

        List<Question> allQuestions = questionMapper.selectRandomByAlbum(albumKey, count);
        Collections.shuffle(allQuestions);

        List<Question> finalQuestions = allQuestions.subList(0,
                Math.min(allQuestions.size(), count));

        String roundId = UUID.randomUUID().toString();

        Map<Long, String> answerMap = finalQuestions.stream()
                .collect(Collectors.toMap(Question::getId, Question::getCorrectOption));
        cacheManager.put(roundId, new GameRoundCache(answerMap));

        List<QuestionDTO> questionDTOs = QuestionAssembler.toDTOList(finalQuestions);
        RoundDTO roundDTO = new RoundDTO();
        roundDTO.setRoundId(roundId);
        roundDTO.setQuestions(questionDTOs);

        log.info("专辑模式: roundId={}, album={}, 题目数={}", roundId, albumKey, questionDTOs.size());
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
    public int calculateScore(int correctCount, int totalQuestions) {
        return correctCount * 10;
    }

    @Override
    public LevelInfo evaluateLevel(int correctCount) {
        return FanLevel.fromScore(correctCount);
    }

    @Override
    public List<PostSubmitHook> getPostSubmitHooks() {
        return List.of(albumUnlockHook);
    }
}
