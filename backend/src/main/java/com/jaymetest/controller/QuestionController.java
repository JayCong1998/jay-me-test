package com.jaymetest.controller;

import com.jaymetest.model.dto.*;
import com.jaymetest.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 题目 API
 */
@Tag(name = "题目接口")
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "随机抽取一局题目")
    @GetMapping("/round")
    public R<RoundDTO> getRound(@RequestParam(defaultValue = "10") int count) {
        RoundDTO round = questionService.generateRound(count);
        return R.ok(round);
    }

    @Operation(summary = "校验单题答案")
    @PostMapping("/check")
    public R<AnswerResultDTO> checkAnswer(@Valid @RequestBody AnswerRequest request) {
        AnswerResultDTO result = questionService.checkAnswer(
                request.getRoundId(), request.getQuestionId(), request.getSelectedOption());
        return R.ok(result);
    }

    @Operation(summary = "使用复活机会")
    @PostMapping("/revive")
    public R<Map<String, Object>> revive(@Valid @RequestBody ReviveRequest request) {
        // 复活：返回正确答案，前端可据此让用户重新作答
        String correctAnswer = questionService.getCorrectAnswer(request.getRoundId(), request.getQuestionId());
        return R.ok(Map.of(
                "revived", true,
                "remainingRevivals", 0
        ));
    }
}
