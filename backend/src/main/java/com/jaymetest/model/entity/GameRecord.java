package com.jaymetest.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 游戏记录实体
 */
@Data
@TableName("game_record")
public class GameRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Round UUID，去重 */
    private String roundId;

    /** 游戏模式: CLASSIC | ALBUM | ABYSS */
    private String mode;

    /** 专辑模式下的专辑标识 */
    private String albumKey;

    /** 用户ID（游客为NULL） */
    private Long userId;

    /** 昵称快照 */
    private String nickname;

    /** 题目总数 */
    private Integer totalQuestions;

    /** 答对数量 */
    private Integer correctCount;

    /** 答题总用时（秒） */
    private Integer timeSpentSecs;

    /** 是否使用复活: 0=未使用 1=已使用 */
    private Integer usedRevival;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
