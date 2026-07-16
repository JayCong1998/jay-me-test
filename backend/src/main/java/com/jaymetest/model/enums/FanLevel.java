package com.jaymetest.model.enums;

import lombok.Getter;

/**
 * 粉丝等级枚举 (5 级)
 */
@Getter
public enum FanLevel {
    PASSERBY("路人粉", "🌱 路人粉", "刚刚路过，杰伦的歌还等你发现", 0, 2),
    JUNIOR("初级杰迷", "🎤 初级杰迷", "入门粉丝，继续加油解锁更多杰伦冷知识", 3, 4),
    INTERMEDIATE("中级杰迷", "🎧 中级杰迷", "资深听友，离骨灰粉只差一张专辑的距离", 5, 6),
    SENIOR("高级杰迷", "🏆 高级杰迷", "铁粉认证，演唱会前排选手就是你", 7, 8),
    ULTIMATE("终极杰迷", "👑 终极杰迷", "你就是杰伦的\"编外制作人\"！无可挑剔", 9, 10);

    private final String name;
    private final String title;
    private final String description;
    private final int minScore;
    private final int maxScore;

    FanLevel(String name, String title, String description, int minScore, int maxScore) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    /**
     * 根据答对题数获取对应等级
     */
    public static FanLevel fromScore(int correctCount) {
        for (FanLevel level : values()) {
            if (correctCount >= level.minScore && correctCount <= level.maxScore) {
                return level;
            }
        }
        return PASSERBY;
    }
}
