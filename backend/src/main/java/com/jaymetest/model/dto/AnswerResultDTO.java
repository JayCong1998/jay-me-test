package com.jaymetest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 答案校验结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultDTO {
    private boolean correct;
    private String correctOption;
    private String explanation;
}
