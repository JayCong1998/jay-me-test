package com.jaymetest.service.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbyssDifficultyPolicyTest {

    private AbyssDifficultyConfig config;

    @BeforeEach
    void setUp() {
        config = new AbyssDifficultyConfig(List.of(
                tier(0, 2, weights("EASY", 1.0)),
                tier(3, 5, weights("EASY", 0.3, "MEDIUM", 0.7)),
                tier(6, 9, weights("MEDIUM", 0.4, "HARD", 0.6)),
                tier(10, 14, weights("MEDIUM", 0.3, "HARD", 0.7)),
                tier(15, 19, weights("HARD", 1.0)),
                tier(20, 29, weights("HARD", 0.8, "ANY", 0.2)),
                tier(30, null, weights("HARD", 0.5, "ANY", 0.5))
        ));
        config.validate();
    }

    @ParameterizedTest
    @CsvSource({
            "0,EASY", "2,EASY",
            "3,MEDIUM", "5,MEDIUM",
            "6,HARD", "9,HARD",
            "10,HARD", "14,HARD",
            "15,HARD", "19,HARD",
            "20,ANY", "29,ANY",
            "30,ANY", "100,ANY"
    })
    void selectsConfiguredTierAtEveryBoundary(int streak, DifficultySelection expected) {
        AbyssDifficultyPolicy policy = new AbyssDifficultyPolicy(config, () -> 0.99);

        assertEquals(expected, policy.select(streak));
    }

    @Test
    void usesCumulativeWeightBoundary() {
        assertEquals(DifficultySelection.EASY,
                new AbyssDifficultyPolicy(config, () -> 0.299999).select(3));
        assertEquals(DifficultySelection.MEDIUM,
                new AbyssDifficultyPolicy(config, () -> 0.3).select(3));
        assertEquals(DifficultySelection.HARD,
                new AbyssDifficultyPolicy(config, () -> 0.799999).select(20));
        assertEquals(DifficultySelection.ANY,
                new AbyssDifficultyPolicy(config, () -> 0.8).select(20));
    }

    private AbyssDifficultyConfig.Tier tier(int from, Integer to, Map<String, Double> weights) {
        return new AbyssDifficultyConfig.Tier(from, to, weights);
    }

    private Map<String, Double> weights(Object... values) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            result.put((String) values[i], (Double) values[i + 1]);
        }
        return result;
    }
}
