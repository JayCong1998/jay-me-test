package com.jaymetest.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 专辑闯关进度实体
 */
@Data
@TableName("album_progress")
public class AlbumProgress implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 专辑标识 */
    private String albumKey;

    /** 0=未解锁 1=已解锁 */
    private Integer unlocked;

    /** 最佳答对数 (0-10) */
    private Integer bestScore;

    /** 总挑战次数 */
    private Integer totalAttempts;

    /** 首次通关时间 (≥UNLOCK_THRESHOLD) */
    private String firstPassedAt;

    /** 最近挑战时间 */
    private String lastAttemptedAt;

    /** 创建时间 */
    private String createdAt;

    /** 更新时间 */
    private String updatedAt;
}
