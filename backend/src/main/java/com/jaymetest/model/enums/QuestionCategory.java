package com.jaymetest.model.enums;

import lombok.Getter;

/**
 * 题目分类枚举
 */
@Getter
public enum QuestionCategory {
    LYRICS("歌词类"),
    ALBUM("专辑归属类");

    private final String description;

    QuestionCategory(String description) {
        this.description = description;
    }
}
