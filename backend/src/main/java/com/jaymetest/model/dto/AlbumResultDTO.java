package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 专辑闯关结果 DTO（嵌入 GameResultDTO）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlbumResultDTO {
    /** 当前专辑标识 */
    private String albumKey;

    /** 当前专辑中文名 */
    private String albumDisplayName;

    /** 是否达到当前专辑通关正确率阈值 */
    private boolean passed;

    /** 本专辑历史最佳成绩 */
    private int albumBestScore;

    /** 本次是否刷新纪录 */
    private boolean isNewRecord;

    /** 是否解锁了下一张专辑 */
    private boolean unlockedNext;

    /** 下一张专辑标识（null 表示没有/已是最后一张） */
    private String nextAlbumKey;

    /** 下一张专辑中文名 */
    private String nextAlbumDisplayName;
}
