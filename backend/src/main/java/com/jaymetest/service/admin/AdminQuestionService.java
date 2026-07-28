package com.jaymetest.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.model.admin.AdminQuestionRequest;
import com.jaymetest.model.admin.PageResponse;
import com.jaymetest.model.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public Question update(long id, AdminQuestionRequest request) {
        Question question = toEntity(new Question(), request);
        question.setId(id);
        questionMapper.updateById(question);
        return questionMapper.selectById(id);
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
}
