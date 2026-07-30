package com.jaymetest.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.admin.AdminQuestionRequest;
import com.jaymetest.model.admin.PageResponse;
import com.jaymetest.model.admin.QuestionOptionRebalanceResponse;
import com.jaymetest.model.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AdminQuestionService {

    private final QuestionMapper questionMapper;

    public PageResponse<Question> list(String keyword, String category, String difficulty, String album, int page, int size) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String like = keyword.trim();
            wrapper.and(w -> w.like(Question::getQuestionText, like)
                    .or().like(Question::getOptionA, like)
                    .or().like(Question::getOptionB, like)
                    .or().like(Question::getOptionC, like)
                    .or().like(Question::getOptionD, like));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq(Question::getCategory, category.trim());
        }
        if (StringUtils.hasText(difficulty)) {
            wrapper.eq(Question::getDifficulty, difficulty.trim());
        }
        if (StringUtils.hasText(album)) {
            wrapper.eq(Question::getAlbum, album.trim());
        }
        wrapper.orderByDesc(Question::getId);

        Page<Question> result = questionMapper.selectPage(new Page<>(safePage(page), safeSize(size)), wrapper);
        return PageResponse.<Question>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .build();
    }

    public Question create(AdminQuestionRequest request) {
        Question question = toEntity(new Question(), request);
        questionMapper.insert(question);
        return question;
    }

    public Question get(long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(404, "题目不存在");
        }
        return question;
    }

    public Question update(long id, AdminQuestionRequest request) {
        Question question = toEntity(new Question(), request);
        question.setId(id);
        questionMapper.updateById(question);
        return questionMapper.selectById(id);
    }

    @Transactional
    public QuestionOptionRebalanceResponse rebalanceOptions() {
        List<Question> questions = new ArrayList<>(questionMapper.selectList(null));
        Map<String, Long> answerDistribution = countAnswers(questions);
        Collections.shuffle(questions);

        int adjustedCount = 0;
        for (Question question : questions) {
            String sourceOption = question.getCorrectOption();
            String targetOption = selectTargetOption(sourceOption, answerDistribution);
            if (targetOption == null) {
                continue;
            }

            swapOptions(question, sourceOption, targetOption);
            answerDistribution.compute(sourceOption, (key, count) -> count - 1);
            answerDistribution.compute(targetOption, (key, count) -> count + 1);
            questionMapper.updateById(question);
            adjustedCount++;
        }

        return new QuestionOptionRebalanceResponse(adjustedCount, Map.copyOf(answerDistribution));
    }

    private Question toEntity(Question question, AdminQuestionRequest request) {
        question.setCategory(request.getCategory());
        question.setAlbum(StringUtils.hasText(request.getAlbum()) ? request.getAlbum().trim() : null);
        question.setDifficulty(request.getDifficulty());
        question.setQuestionText(request.getQuestionText().trim());
        question.setOptionA(request.getOptionA().trim());
        question.setOptionB(request.getOptionB().trim());
        question.setOptionC(request.getOptionC().trim());
        question.setOptionD(request.getOptionD().trim());
        question.setCorrectOption(request.getCorrectOption());
        question.setExplanation(request.getExplanation().trim());
        return question;
    }

    private long safePage(int page) {
        return Math.max(page, 1);
    }

    private long safeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private Map<String, Long> countAnswers(List<Question> questions) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String option : answerOptions()) {
            counts.put(option, 0L);
        }
        for (Question question : questions) {
            counts.computeIfPresent(question.getCorrectOption(), (option, count) -> count + 1);
        }
        return counts;
    }

    private String selectTargetOption(String sourceOption, Map<String, Long> counts) {
        if (!counts.containsKey(sourceOption)) {
            return null;
        }
        long maximum = Collections.max(counts.values());
        long minimum = Collections.min(counts.values());
        if (maximum - minimum <= 1 || counts.get(sourceOption) != maximum) {
            return null;
        }

        List<String> targets = counts.entrySet().stream()
                .filter(entry -> entry.getValue() == minimum)
                .map(Map.Entry::getKey)
                .toList();
        return targets.get(ThreadLocalRandom.current().nextInt(targets.size()));
    }

    private void swapOptions(Question question, String sourceOption, String targetOption) {
        String sourceText = getOptionText(question, sourceOption);
        setOptionText(question, sourceOption, getOptionText(question, targetOption));
        setOptionText(question, targetOption, sourceText);
        question.setCorrectOption(targetOption);
    }

    private String getOptionText(Question question, String option) {
        return switch (option) {
            case "A" -> question.getOptionA();
            case "B" -> question.getOptionB();
            case "C" -> question.getOptionC();
            case "D" -> question.getOptionD();
            default -> throw new IllegalArgumentException("Unsupported option: " + option);
        };
    }

    private void setOptionText(Question question, String option, String text) {
        switch (option) {
            case "A" -> question.setOptionA(text);
            case "B" -> question.setOptionB(text);
            case "C" -> question.setOptionC(text);
            case "D" -> question.setOptionD(text);
            default -> throw new IllegalArgumentException("Unsupported option: " + option);
        }
    }

    private List<String> answerOptions() {
        return Arrays.asList("A", "B", "C", "D");
    }
}
