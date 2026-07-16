package com.jaymetest.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 复活请求
 */
@Data
public class ReviveRequest {
    @NotNull(message = "roundId 不能为空")
    private String roundId;

    @NotNull(message = "questionId 不能为空")
    private Long questionId;
}
