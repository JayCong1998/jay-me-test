package com.jaymetest.service.game;

import com.jaymetest.model.dto.GameResultDTO;
import com.jaymetest.model.dto.GameSubmitRequest;

/**
 * 游戏提交后置处理钩子。
 * 每个钩子只处理自己关心的模式/条件，实现单一职责。
 *
 * <p>使用方式：在策略的 {@link GameStrategy#getPostSubmitHooks()} 中注册。</p>
 */
@FunctionalInterface
public interface PostSubmitHook {

    /**
     * 在游戏结果已计算、GameResultDTO 构建完成前执行。
     * 可通过 builder 追加模式特有字段（如 albumResult）。
     *
     * @param request 提交请求
     * @param builder GameResultDTO 构建器（已填充通用字段）
     * @param userId  登录用户 ID（游客为 null）
     */
    void afterSubmit(GameSubmitRequest request, GameResultDTO.GameResultDTOBuilder builder, Long userId);
}
