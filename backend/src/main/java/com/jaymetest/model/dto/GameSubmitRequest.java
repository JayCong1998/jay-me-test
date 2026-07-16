package com.jaymetest.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 游戏结果提交请求
 */
@Data
public class GameSubmitRequest {
    @NotNull(message = "roundId 不能为空")
    private String roundId;

    @NotNull(message = "correctCount 不能为空")
    private Integer correctCount;

    @NotNull(message = "timeSpentSecs 不能为空")
    private Integer timeSpentSecs;

    private Integer usedRevival = 0;

    /** 昵称快照（游客时前端生成 "杰迷XXXX"） */
    private String nickname;
}
