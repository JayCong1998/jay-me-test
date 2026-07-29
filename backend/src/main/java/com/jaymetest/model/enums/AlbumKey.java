package com.jaymetest.model.enums;

import lombok.Getter;

/**
 * 周杰伦 16 张录音室专辑枚举（按发行时间排序）
 */
@Getter
public enum AlbumKey {
    JAY("Jay", 2000),
    FANTASY("范特西", 2001),
    EIGHT_DIMENSIONS("八度空间", 2002),
    YE_HUI_MEI("叶惠美", 2003),
    SEVEN_SCENT("七里香", 2004),
    NOVEMBER_CHOPIN("十一月的肖邦", 2005),
    STILL_FANTASY("依然范特西", 2006),
    ON_THE_RUN("我很忙", 2007),
    CAPRICORN("魔杰座", 2008),
    THE_ERA("跨时代", 2010),
    EXCLAMATION_MARK("惊叹号", 2011),
    OPUS_12("12新作", 2012),
    AIYO_NOT_BAD("哎呦不错哦", 2014),
    BEDTIME_STORIES("周杰伦的床边故事", 2016),
    GREATEST_WORKS("最伟大的作品", 2022),
    SUN_CHILD("太阳之子", 2026);

    /** 显示名称 */
    private final String displayName;

    /** 发行年份 */
    private final int year;

    AlbumKey(String displayName, int year) {
        this.displayName = displayName;
        this.year = year;
    }

    /** 根据显示名称反向查找枚举（API 入参解析用） */
    public static AlbumKey fromDisplayName(String displayName) {
        for (AlbumKey key : values()) {
            if (key.displayName.equals(displayName)) return key;
        }
        throw new IllegalArgumentException("未知专辑名: " + displayName);
    }

    /** 通关门槛：答对 8/10 解锁下一关 */
    public static final int UNLOCK_THRESHOLD = 8;

    /** 第一张专辑（新用户默认解锁） */
    public static AlbumKey first() {
        return values()[0];
    }

    /** 下一张专辑（按发行顺序），null 表示最后一张 */
    public AlbumKey next() {
        AlbumKey[] all = values();
        int nextOrdinal = this.ordinal() + 1;
        return nextOrdinal < all.length ? all[nextOrdinal] : null;
    }

    /** 上一张专辑（按发行顺序），null 表示第一张 */
    public AlbumKey previous() {
        int prevOrdinal = this.ordinal() - 1;
        return prevOrdinal >= 0 ? values()[prevOrdinal] : null;
    }

    /** 是否为第一张专辑 */
    public boolean isFirst() {
        return this.ordinal() == 0;
    }

    /** 是否为最后一张专辑 */
    public boolean isLast() {
        return this.ordinal() == values().length - 1;
    }
}
