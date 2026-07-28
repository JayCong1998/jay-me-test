package com.jaymetest.model.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminRecordDTO {
    private Long id;
    private String roundId;
    private String mode;
    private String albumKey;
    private Long userId;
    private String nickname;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer timeSpentSecs;
    private Integer usedRevival;
    private LocalDateTime createdAt;
}
