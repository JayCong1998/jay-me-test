package com.jaymetest.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jaymetest.model.dto.*;
import com.jaymetest.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计 API
 */
@Tag(name = "统计接口")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "提交游戏结果")
    @PostMapping("/submit")
    public R<GameResultDTO> submitResult(@Valid @RequestBody GameSubmitRequest request) {
        GameResultDTO result = statsService.submitResult(request);
        return R.ok(result);
    }

    @Operation(summary = "全局统计概览")
    @GetMapping("/overview")
    public R<StatsOverviewDTO> getOverview() {
        StatsOverviewDTO overview = statsService.getOverview();
        return R.ok(overview);
    }

    @Operation(summary = "获取当前用户的考试记录")
    @GetMapping("/my-records")
    @SaCheckLogin
    public R<List<GameRecordDTO>> getMyRecords() {
        return R.ok(statsService.getMyRecords());
    }
}
