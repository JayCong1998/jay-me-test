package com.jaymetest.service.game;

import com.jaymetest.config.LevelRangeProperties;
import java.util.List;

final class LevelEvaluator {
    private LevelEvaluator() {}
    static LevelInfo evaluate(int value, List<LevelRangeProperties> levels) {
        return levels.stream().filter(level -> value >= level.getMin() && (level.getMax() == null || value <= level.getMax())).findFirst()
                .map(level -> new ConfiguredLevel(level.getKey(), level.getTitle(), level.getDescription()))
                .orElseThrow(() -> new IllegalStateException("没有匹配的等级配置"));
    }
}
