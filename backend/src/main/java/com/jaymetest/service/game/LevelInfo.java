package com.jaymetest.service.game;

/**
 * 统一等级接口 — FanLevel 和 AbyssLevel 共同抽象
 */
public interface LevelInfo {

    /** 等级枚举名（如 PASSERBY、ABYSS_GOD） */
    String name();

    /** 等级称号（如 "👑 终极杰迷"、"深渊之神"） */
    String getTitle();

    /** 等级描述 */
    String getDescription();
}
