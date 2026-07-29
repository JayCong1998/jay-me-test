package com.jaymetest.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jaymetest.model.dto.*;
import com.jaymetest.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 无尽深渊模式 API。
 * 内部委托给 {@code AbyssGameStrategy}。
 */
@Tag(name = "无尽深渊接口")
@RestController
@RequestMapping("/api/abyss")
@RequiredArgsConstructor
@SaCheckLogin
public class AbyssController {

    private final QuestionService questionService;

    @Operation(summary = "开始深渊挑战，返回首批题目")
    @PostMapping("/start")
    public R<AbyssStepDTO> startAbyss() {
        AbyssStepDTO result = questionService.abyss()
                .generateStart(questionService.cacheManager());
        return R.ok(result);
    }

    @Operation(summary = "获取下一批题目（前端静默预加载）")
    @PostMapping("/batch")
    public R<AbyssStepDTO> nextBatch(@Valid @RequestBody AbyssBatchRequest request) {
        AbyssStepDTO result = questionService.abyss()
                .generateBatch(request.getRoundId(), questionService.cacheManager());
        return R.ok(result);
    }

    @Operation(summary = "深渊模式校验答案（答对自动累加 streak）")
    @PostMapping("/check")
    public R<AnswerResultDTO> checkAnswer(@Valid @RequestBody AnswerRequest request) {
        AnswerResultDTO result = questionService.abyss()
                .checkAnswer(request.getRoundId(), request.getQuestionId(),
                        request.getSelectedOption(), questionService.cacheManager());
        return R.ok(result);
    }

    @Operation(summary = "消耗一次深渊续命机会并重答当前题")
    @PostMapping("/revive")
    public R<java.util.Map<String, Object>> revive(@Valid @RequestBody ReviveRequest request) {
        questionService.abyss().revive(request.getRoundId(), request.getQuestionId(), questionService.cacheManager());
        int remainingRevivals = questionService.abyss()
                .getRemainingRevivals(request.getRoundId(), questionService.cacheManager());
        return R.ok(java.util.Map.of("revived", true, "remainingRevivals", remainingRevivals));
    }
}
