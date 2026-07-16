package com.jaymetest.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 答题请求
 */
@Data
public class AnswerRequest {
    @NotNull(message = "roundId 不能为空")
    private String roundId;

    @NotNull(message = "questionId 不能为空")
    private Long questionId;

    @NotNull(message = "selectedOption 不能为空")
    private String selectedOption;
}
