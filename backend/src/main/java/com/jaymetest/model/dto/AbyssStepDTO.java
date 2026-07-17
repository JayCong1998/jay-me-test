package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 深渊模式 — 响应（start 和 batch 共用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbyssStepDTO {
    private String roundId;
    private List<QuestionDTO> questions;
    private int streak;
}
