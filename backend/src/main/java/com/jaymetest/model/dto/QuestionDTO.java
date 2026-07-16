package com.jaymetest.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 题目 DTO（不含正确答案）
 */
@Data
public class QuestionDTO {
    private Long id;
    private String category;
    private String difficulty;
    private String questionText;
    private List<String> options;
}
