package com.jaymetest.service.game.strategy;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.QuestionDTO;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.model.entity.Question;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.cache.GameRoundCache;
import com.jaymetest.service.game.cache.RoundCacheManager;
import com.jaymetest.service.game.support.QuestionAssembler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractFixedRoundGameStrategy implements FixedRoundStrategy {

    protected final QuestionMapper questionMapper;

    protected AbstractFixedRoundGameStrategy(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    @Override
    public RoundDTO generateRound(String albumKey, RoundCacheManager cacheManager) {
        List<Question> finalQuestions = selectQuestions(albumKey);
        String roundId = UUID.randomUUID().toString();
        Map<Long, String> answerMap = finalQuestions.stream()
                .collect(Collectors.toMap(Question::getId, Question::getCorrectOption));
        cacheManager.put(roundId, new GameRoundCache(getMode(), roundAlbumKey(albumKey), answerMap));

        List<QuestionDTO> questionDTOs = QuestionAssembler.toDTOList(finalQuestions);
        RoundDTO roundDTO = new RoundDTO();
        roundDTO.setRoundId(roundId);
        roundDTO.setQuestions(questionDTOs);
        return roundDTO;
    }

    @Override
    public AnswerResultDTO checkAnswer(String roundId, Long questionId, String selectedOption,
                                       RoundCacheManager cacheManager) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);
        String correctOption = cache.getAnswerMap().get(questionId);
        if (correctOption == null) {
            throw new BusinessException(404, "题目不属于该回合");
        }

        boolean correct = correctOption.equalsIgnoreCase(selectedOption.trim().toUpperCase());
        cache.recordAnswer(questionId, correct);
        Question question = questionMapper.selectById(questionId);

        return AnswerResultDTO.builder()
                .correct(correct)
                .correctOption(correctOption)
                .explanation(question != null ? question.getExplanation() : "暂无解析")
                .build();
    }

    @Override
    public int calculateScore(int correctCount, int totalQuestions) {
        return totalQuestions == 0 ? 0 : (int) Math.round(correctCount * 100.0 / totalQuestions);
    }

    protected String roundAlbumKey(String albumKey) {
        return getMode() == GameMode.ALBUM ? albumKey : null;
    }

    protected int fixedScorePercent(int correctCount, int questionCount) {
        return questionCount == 0 ? 0 : correctCount * 100 / questionCount;
    }

    protected abstract List<Question> selectQuestions(String albumKey);
}
