package com.jaymetest.model.enums;

import lombok.Getter;

/**
 * 难度等级枚举
 */
@Getter
public enum DifficultyLevel {
    EASY("简单"),
    MEDIUM("中等"),
    HARD("困难");

    private final String description;

    DifficultyLevel(String description) {
        this.description = description;
    }
}
