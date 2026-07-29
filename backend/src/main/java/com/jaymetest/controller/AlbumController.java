package com.jaymetest.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.jaymetest.model.dto.AlbumDTO;
import com.jaymetest.model.dto.R;
import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.service.AlbumProgressService;
import com.jaymetest.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专辑闯关 API（需登录）。
 * 内部委托给 {@code AlbumGameStrategy}。
 */
@Tag(name = "专辑闯关接口")
@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
@SaCheckLogin
public class AlbumController {

    private final AlbumProgressService albumProgressService;
    private final QuestionService questionService;

    @Operation(summary = "获取专辑列表及解锁状态")
    @GetMapping("/list")
    public R<List<AlbumDTO>> getAlbumList() {
        long userId = StpUtil.getLoginIdAsLong();
        return R.ok(albumProgressService.getAlbumList(userId));
    }

    @Operation(summary = "获取专辑关卡题目")
    @GetMapping("/round")
    public R<RoundDTO> getAlbumRound(@RequestParam String albumKey) {
        long userId = StpUtil.getLoginIdAsLong();
        albumProgressService.canAccessAlbum(userId, albumKey);
        RoundDTO round = questionService.album()
                .generateRound(albumKey, questionService.cacheManager());
        return R.ok(round);
    }
}
