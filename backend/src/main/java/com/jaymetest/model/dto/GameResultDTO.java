package com.jaymetest.model.dto;

import com.jaymetest.model.enums.GameMode;
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
    /** 单局唯一标识 */
    private String roundId;

    /** 游戏模式 */
    private GameMode mode;

    /** 专辑标识（仅 ALBUM 模式非 null） */
    private String albumKey;

    /** 策略得分：经典/专辑为百分制，深渊为连续答对数 */
    private int score;

    /** 答对数量 */
    private int correctCount;

    /** 总题数 */
    private int totalQuestions;

    /** 正确率 */
    private double accuracy;

    /** 答题用时（秒） */
    private int timeSpentSecs;

    /** 是否使用复活 */
    private boolean usedRevival;

    /** 结果创建时间（ISO 8601） */
    private String createdAt;

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

    /** 专辑闯关结果（仅 ALBUM 模式非 null） */
    private AlbumResultDTO albumResult;
}
