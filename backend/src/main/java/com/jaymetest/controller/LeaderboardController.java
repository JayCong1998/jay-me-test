package com.jaymetest.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jaymetest.model.dto.LeaderboardResult;
import com.jaymetest.model.dto.R;
import com.jaymetest.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 排行榜 API
 */
@Tag(name = "排行榜接口")
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "获取排行榜")
    @GetMapping
    @SaCheckLogin
    public R<LeaderboardResult> getLeaderboard(
            @Parameter(description = "排行类型: total / daily / level") @RequestParam(defaultValue = "total") String type,
            @Parameter(description = "返回条数") @RequestParam(defaultValue = "50") int limit,
            @Parameter(description = "等级名（type=level 时必填）") @RequestParam(required = false) String level) {

        LeaderboardResult result;
        switch (type) {
            case "daily":
                result = leaderboardService.getDailyLeaderboard(limit);
                break;
            case "level":
                if (level == null || level.isEmpty()) {
                    return R.fail(400, "level 榜需要指定 level 参数");
                }
                result = leaderboardService.getLevelLeaderboard(level, limit);
                break;
            default:
                result = leaderboardService.getTotalLeaderboard(limit);
        }
        return R.ok(result);
    }
}
