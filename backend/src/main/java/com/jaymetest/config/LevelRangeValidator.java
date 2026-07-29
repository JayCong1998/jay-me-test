package com.jaymetest.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class LevelRangeValidator {
    private LevelRangeValidator() {}

    static void validate(List<LevelRangeProperties> levels, int terminalMax, boolean openEndedLast, String mode) {
        if (levels == null || levels.isEmpty()) throw new IllegalStateException(mode + " 至少配置一个等级");
        Set<String> keys = new HashSet<>();
        int expectedMin = 0;
        for (int i = 0; i < levels.size(); i++) {
            LevelRangeProperties level = levels.get(i);
            boolean last = i == levels.size() - 1;
            if (level == null || level.getKey() == null || !level.getKey().matches("[A-Z0-9_]+") || !keys.add(level.getKey())) throw new IllegalStateException(mode + " 等级 key 非法或重复");
            if (level.getTitle() == null || level.getTitle().isBlank() || level.getDescription() == null || level.getDescription().isBlank()) throw new IllegalStateException(mode + " 等级标题和描述不能为空");
            if (level.getMin() == null || level.getMin() != expectedMin) throw new IllegalStateException(mode + " 等级区间必须从 0 开始且连续无重叠");
            if (last && openEndedLast) {
                if (level.getMax() != null) throw new IllegalStateException(mode + " 最后一档必须无上限");
                return;
            }
            if (level.getMax() == null || level.getMax() < level.getMin()) throw new IllegalStateException(mode + " 等级 max 非法");
            expectedMin = level.getMax() + 1;
        }
        if (expectedMin != terminalMax + 1) throw new IllegalStateException(mode + " 等级区间必须完整覆盖 0-" + terminalMax);
    }
}
