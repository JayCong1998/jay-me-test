package com.jaymetest.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.jaymetest.model.dto.GameRecordDTO;
import com.jaymetest.model.dto.R;
import com.jaymetest.service.GameRecordQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/game-records")
@RequiredArgsConstructor
@SaCheckLogin
public class GameRecordController {
    private final GameRecordQueryService gameRecordQueryService;
    @GetMapping("/me")
    public R<List<GameRecordDTO>> getMyRecords(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return R.ok(gameRecordQueryService.getMyRecords(page, size));
    }
}
