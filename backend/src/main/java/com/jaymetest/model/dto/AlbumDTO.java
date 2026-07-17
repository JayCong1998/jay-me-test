package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 专辑信息 DTO（返回给前端列表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {
    /** 专辑标识 */
    private String albumKey;

    /** 中文名 */
    private String displayName;

    /** 发行年份 */
    private int year;

    /** 是否已解锁 */
    private boolean unlocked;

    /** 最佳答对数 (0-10) */
    private int bestScore;

    /** 总挑战次数 */
    private int totalAttempts;

    /** 是否为第一张（默认解锁） */
    private boolean isFirst;

    /** 是否为最后一张 */
    private boolean isLast;
}
