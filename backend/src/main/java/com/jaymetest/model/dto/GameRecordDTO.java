package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户考试记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRecordDTO {
    private String roundId;
    private int correctCount;
    private int totalQuestions;
    private int timeSpentSecs;
    private boolean usedRevival;
    private String createdAt;
    private String level;
    private String levelTitle;
}
