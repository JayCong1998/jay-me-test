package com.jaymetest.model.enums;

import com.jaymetest.service.game.LevelInfo;
import lombok.Getter;

/**
 * 无尽深渊等级枚举（基于连续答对 streak）
 */
@Getter
public enum AbyssLevel implements LevelInfo {
    ABYSS_TOURIST(0, 2, "深渊游客", "初入深渊，浅尝辄止"),
    ABYSS_EXPLORER(3, 5, "深渊探索者", "开始深入杰伦的音乐迷宫"),
    ABYSS_WARRIOR(6, 10, "深渊战士", "已经击败大多数杰迷"),
    ABYSS_KNIGHT(11, 20, "深渊骑士", "铁粉中的铁粉"),
    ABYSS_LORD(21, 30, "深渊领主", "杰伦编年史活字典"),
    ABYSS_OVERLORD(31, 50, "深渊霸主", "制作人都要请教你的程度"),
    ABYSS_GOD(51, Integer.MAX_VALUE, "深渊之神", "传说的缔造者，杰伦本伦？");

    private final int minStreak;
    private final int maxStreak;
    private final String title;
    private final String description;

    AbyssLevel(int minStreak, int maxStreak, String title, String description) {
        this.minStreak = minStreak;
        this.maxStreak = maxStreak;
        this.title = title;
        this.description = description;
    }

    /** 根据 streak 获取等级 */
    public static AbyssLevel fromStreak(int streak) {
        for (AbyssLevel level : values()) {
            if (streak >= level.minStreak && streak <= level.maxStreak) {
                return level;
            }
        }
        return ABYSS_TOURIST;
    }
}
