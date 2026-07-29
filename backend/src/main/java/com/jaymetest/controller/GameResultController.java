package com.jaymetest.controller;

import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;
import com.jaymetest.model.dto.R;
import com.jaymetest.service.GameResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game-results")
@RequiredArgsConstructor
public class GameResultController {
    private final GameResultService gameResultService;
    @PostMapping
    public R<GameResultDTO> submit(@Valid @RequestBody GameSubmitRequest request) { return R.ok(gameResultService.submitResult(request)); }
}
