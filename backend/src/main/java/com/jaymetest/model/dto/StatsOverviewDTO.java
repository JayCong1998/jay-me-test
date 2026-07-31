package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 全局统计概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsOverviewDTO {
    private long totalPlayers;
    private long totalGames;
    private double averageScore;
    private int maxAbyssStreak;
    private Map<String, Double> levelDistribution;
}
