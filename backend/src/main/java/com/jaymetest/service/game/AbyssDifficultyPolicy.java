package com.jaymetest.service.game;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AbyssDifficultyPolicy {

    private final AbyssDifficultyConfig config;
    private final RandomSource randomSource;

    public DifficultySelection select(int streak) {
        if (streak < 0) {
            throw new IllegalArgumentException("streak 不能为负数");
        }

        AbyssDifficultyConfig.Tier tier = config.tiers().stream()
                .filter(candidate -> candidate.contains(streak))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("没有覆盖 streak=" + streak + " 的深渊难度档位"));

        double randomValue = randomSource.nextDouble();
        double cumulative = 0.0;
        DifficultySelection lastSelection = null;
        for (Map.Entry<String, Double> entry : tier.weights().entrySet()) {
            lastSelection = DifficultySelection.valueOf(entry.getKey());
            cumulative += entry.getValue();
            if (randomValue < cumulative) {
                return lastSelection;
            }
        }
        // 权重小数相加可能出现 0.999999 这类精度尾差，落到最后一个选项比直接失败更符合配置语义。
        return lastSelection;
    }
}
