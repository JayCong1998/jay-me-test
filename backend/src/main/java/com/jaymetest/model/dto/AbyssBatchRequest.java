package com.jaymetest.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 深渊模式 — 获取下一批题目请求
 */
@Data
public class AbyssBatchRequest {
    @NotNull(message = "roundId 不能为空")
    private String roundId;
}
