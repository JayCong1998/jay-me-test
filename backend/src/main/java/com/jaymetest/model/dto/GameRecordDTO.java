package com.jaymetest.model.dto;

import com.jaymetest.model.enums.GameMode;
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
    private GameMode mode;
    private String albumKey;
    private int score;
    private int correctCount;
    private int totalQuestions;
    private int timeSpentSecs;
    private boolean usedRevival;
    private String createdAt;
    private String level;
    private String levelTitle;
}
