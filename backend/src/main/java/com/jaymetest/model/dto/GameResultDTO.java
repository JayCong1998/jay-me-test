package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultDTO {
    /** 得分（百分比） */
    private int score;

    /** 答对数量 */
    private int correctCount;

    /** 总题数 */
    private int totalQuestions;

    /** 正确率 */
    private double accuracy;

    /** 答题用时（秒） */
    private int timeSpentSecs;

    /** 等级枚举名 */
    private String level;

    /** 等级称号 */
    private String levelTitle;

    /** 等级描述 */
    private String levelDescription;

    /** 击败百分比 */
    private double beatPercentage;

    /** 总玩家数 */
    private long totalPlayers;
}
