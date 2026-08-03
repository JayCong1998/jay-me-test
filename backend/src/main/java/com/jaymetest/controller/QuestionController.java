package com.jaymetest.controller;

import com.jaymetest.model.dto.AnswerRequest;
import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.model.dto.R;
import com.jaymetest.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题目通用 API 入口，保留服务端单题答案校验。
 */
@Tag(name = "题目通用接口")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "校验单题答案")
    @PostMapping("/check")
    public R<AnswerResultDTO> checkAnswer(@Valid @RequestBody AnswerRequest request) {
        AnswerResultDTO result = questionService.checkAnswer(
                request.getRoundId(), request.getQuestionId(), request.getSelectedOption());
        return R.ok(result);
    }
}
