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

    /** 题目总数（经典/专辑模式默认 10，深渊模式传实际值） */
    private Integer totalQuestions = 10;

    @NotNull(message = "timeSpentSecs 不能为空")
    private Integer timeSpentSecs;

    private Integer usedRevival = 0;

    /** 游戏模式: CLASSIC | ALBUM | ABYSS */
    private String mode = "CLASSIC";

    /** 专辑模式下的专辑标识 */
    private String albumKey;

    /** 昵称快照（游客时前端生成 "杰迷XXXX"） */
    private String nickname;
}
