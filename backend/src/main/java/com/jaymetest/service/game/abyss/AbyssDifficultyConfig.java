package com.jaymetest.service.game.abyss;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * 深渊模式的连续难度档位配置。
 */
@ConfigurationProperties(prefix = "game.abyss.difficulty")
public record AbyssDifficultyConfig(List<Tier> tiers) {

    private static final double WEIGHT_EPSILON = 1.0e-9;

    public record Tier(Integer from, Integer to, Map<String, Double> weights) {
        boolean contains(int streak) {
            return from != null && streak >= from && (to == null || streak <= to);
        }
    }

    @PostConstruct
    public void validate() {
        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalStateException("深渊难度至少需要一个档位");
        }

        int expectedFrom = 0;
        for (int index = 0; index < tiers.size(); index++) {
            Tier tier = tiers.get(index);
            boolean last = index == tiers.size() - 1;
            if (tier == null || tier.from() == null) {
                fail(index, "from 不能为空");
            }
            if (tier.from() != expectedFrom) {
                fail(index, "from 必须为 " + expectedFrom + "，实际为 " + tier.from());
            }
            if (!last && tier.to() == null) {
                fail(index, "只有最后一个档位可以省略 to");
            }
            if (last && tier.to() != null) {
                fail(index, "最后一个档位必须省略 to 以覆盖后续 streak");
            }
            if (tier.to() != null && tier.to() < tier.from()) {
                fail(index, "to 不能小于 from");
            }
            validateWeights(index, tier.weights());
            if (tier.to() != null) {
                expectedFrom = tier.to() + 1;
            }
        }
    }

    private void validateWeights(int index, Map<String, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            fail(index, "weights 不能为空");
        }

        double sum = 0.0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            try {
                DifficultySelection.valueOf(entry.getKey());
            } catch (IllegalArgumentException | NullPointerException exception) {
                fail(index, "未知难度: " + entry.getKey());
            }
            Double weight = entry.getValue();
            if (weight == null || !Double.isFinite(weight) || weight <= 0.0) {
                fail(index, "权重必须为正数: " + entry.getKey());
            }
            sum += weight;
        }
        if (Math.abs(sum - 1.0) > WEIGHT_EPSILON) {
            fail(index, "权重总和必须为 1.0，实际为 " + sum);
        }
    }

    private void fail(int index, String reason) {
        throw new IllegalStateException("深渊难度档位[" + index + "]配置错误: " + reason);
    }
}
