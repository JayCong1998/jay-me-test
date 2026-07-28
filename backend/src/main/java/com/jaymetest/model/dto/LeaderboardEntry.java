package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 排行榜条目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private long rank;
    private String nickname;
    private int correctCount;
    private int timeSpentSecs;
    private String levelTitle;
    private LocalDateTime createdAt;
    private String summaryText;
    private String detailText;
    private String scoreText;
    private Integer completedAlbumCount;
    private Integer totalAlbumTimeSecs;
    private String bestAlbumKey;
    private String bestAlbumName;
    private Integer streak;
}
