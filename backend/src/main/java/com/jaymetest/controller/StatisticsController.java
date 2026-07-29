package com.jaymetest.controller;

import com.jaymetest.model.dto.R;
import com.jaymetest.model.dto.StatsOverviewDTO;
import com.jaymetest.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    @GetMapping("/overview")
    public R<StatsOverviewDTO> getOverview() { return R.ok(statisticsService.getOverview()); }
}
