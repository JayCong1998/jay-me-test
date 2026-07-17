package com.jaymetest.service.game;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 深渊模式难度阶梯配置。
 *
 * <p>对应 {@code application.yml} 中的 {@code game.abyss.difficulty}。</p>
 */
@ConfigurationProperties(prefix = "game.abyss.difficulty")
public record AbyssDifficultyConfig(
    /** streak ≤ 2: 纯 EASY */
    String earlyTier,
    /** streak 3-5: MEDIUM 概率 */
    double midTierMediumProb,
    /** streak 6-9: HARD 概率 */
    double midHardTierHardProb,
    /** streak 10-14: HARD 概率 */
    double hardTierHardProb,
    /** streak 15-19: 纯 HARD */
    String hardTier,
    /** streak 20-29: HARD 概率 */
    double lateTierHardProb,
    /** streak 30+: HARD 概率 */
    String endlessTier
) {
    // 默认值
    public AbyssDifficultyConfig() {
        this("EASY", 0.7, 0.6, 0.7, "HARD", 0.8, "HARD");
    }
}
