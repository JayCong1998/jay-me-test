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

    @Operation(summary = "获取排行榜（分页）")
    @GetMapping
    @SaCheckLogin
    public R<LeaderboardResult> getLeaderboard(
            @Parameter(description = "排行榜类型: classic / album / abyss") @RequestParam(defaultValue = "classic") String type,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {

        int offset = (page - 1) * size;

        LeaderboardResult result;
        switch (type) {
            case "album":
                result = leaderboardService.getAlbumLeaderboard(size, offset);
                break;
            case "abyss":
                result = leaderboardService.getAbyssLeaderboard(size, offset);
                break;
            case "classic":
                result = leaderboardService.getClassicLeaderboard(size, offset);
                break;
            default:
                return R.fail(400, "不支持的排行榜类型");
        }
        return R.ok(result);
    }
}
