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

    @NotNull(message = "timeSpentSecs 不能为空")
    private Integer timeSpentSecs;

    /** 登录用户昵称快照。游客不提交昵称。 */
    private String nickname;

}
