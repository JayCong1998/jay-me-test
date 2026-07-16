package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 排行榜响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResult {
    private List<LeaderboardEntry> list;
    private Long myRank;
}
