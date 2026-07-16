package com.jaymetest.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 一局题目的响应
 */
@Data
public class RoundDTO {
    private String roundId;
    private List<QuestionDTO> questions;
}
