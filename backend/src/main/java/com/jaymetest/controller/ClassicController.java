package com.jaymetest.controller;

import com.jaymetest.model.dto.R;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 经典模式 API 入口。
 */
@Tag(name = "经典模式接口")
@RestController
@RequestMapping("/api/classic")
@RequiredArgsConstructor
public class ClassicController {

    private final QuestionService questionService;

    @Operation(summary = "随机抽取一局经典模式题目")
    @GetMapping("/round")
    public R<RoundDTO> getRound() {
        RoundDTO round = questionService.classic()
                .generateRound(null, questionService.cacheManager());
        return R.ok(round);
    }
}
