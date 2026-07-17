package com.jaymetest.service.game;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.model.enums.GameMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 游戏策略工厂 — 根据 GameMode 返回对应的策略实现。
 * Spring 自动注入所有 {@link GameStrategy} Bean。
 */
@Component
public class GameStrategyFactory {

    private final Map<GameMode, GameStrategy> strategyMap;

    public GameStrategyFactory(List<GameStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(GameStrategy::getMode, Function.identity()));
    }

    /** 根据枚举获取策略 */
    public GameStrategy get(GameMode mode) {
        GameStrategy strategy = strategyMap.get(mode);
        if (strategy == null) {
            throw new BusinessException(400, "不支持的游戏模式: " + mode);
        }
        return strategy;
    }

    /** 根据字符串获取策略（兼容前端传 "CLASSIC"/"ALBUM"/"ABYSS"） */
    public GameStrategy get(String mode) {
        try {
            return get(GameMode.valueOf(mode.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的游戏模式: " + mode);
        }
    }

    /** 解析 mode 字符串为 GameMode 枚举，失败抛异常 */
    public GameMode resolveMode(String modeStr) {
        try {
            return GameMode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的游戏模式: " + modeStr);
        }
    }
}
