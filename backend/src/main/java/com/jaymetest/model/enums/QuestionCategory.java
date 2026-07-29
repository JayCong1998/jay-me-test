package com.jaymetest.model.enums;

import lombok.Getter;

/**
 * 题目分类枚举
 */
@Getter
public enum QuestionCategory {
    LYRICS("歌词"),
    WORKS("作品"),
    SCREEN("影视"),
    KNOWLEDGE("知识");

    private final String description;

    QuestionCategory(String description) {
        this.description = description;
    }
}
