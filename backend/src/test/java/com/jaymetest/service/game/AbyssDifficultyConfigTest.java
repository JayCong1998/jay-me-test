package com.jaymetest.service.game;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbyssDifficultyConfigTest {

    @Test
    void acceptsContiguousTiersCoveringAllStreaks() {
        AbyssDifficultyConfig config = new AbyssDifficultyConfig(List.of(
                tier(0, 2, weights("EASY", 1.0)),
                tier(3, null, weights("HARD", 0.5, "ANY", 0.5))
        ));

        assertDoesNotThrow(config::validate);
    }

    @Test
    void rejectsTierThatDoesNotStartAtZero() {
        AbyssDifficultyConfig config = new AbyssDifficultyConfig(List.of(
                tier(1, null, weights("EASY", 1.0))
        ));

        assertThrows(IllegalStateException.class, config::validate);
    }

    @Test
    void rejectsGapOrOverlapBetweenTiers() {
        AbyssDifficultyConfig gap = new AbyssDifficultyConfig(List.of(
                tier(0, 2, weights("EASY", 1.0)),
                tier(4, null, weights("HARD", 1.0))
        ));
        AbyssDifficultyConfig overlap = new AbyssDifficultyConfig(List.of(
                tier(0, 3, weights("EASY", 1.0)),
                tier(3, null, weights("HARD", 1.0))
        ));

        assertThrows(IllegalStateException.class, gap::validate);
        assertThrows(IllegalStateException.class, overlap::validate);
    }

    @Test
    void rejectsUnboundedTierBeforeLastTier() {
        AbyssDifficultyConfig config = new AbyssDifficultyConfig(List.of(
                tier(0, null, weights("EASY", 1.0)),
                tier(3, null, weights("HARD", 1.0))
        ));

        assertThrows(IllegalStateException.class, config::validate);
    }

    @Test
    void rejectsBoundedLastTier() {
        AbyssDifficultyConfig config = new AbyssDifficultyConfig(List.of(
                tier(0, 2, weights("EASY", 1.0))
        ));

        assertThrows(IllegalStateException.class, config::validate);
    }

    @Test
    void rejectsInvalidWeightsAndUnknownDifficulty() {
        AbyssDifficultyConfig wrongSum = new AbyssDifficultyConfig(List.of(
                tier(0, null, weights("EASY", 0.4, "HARD", 0.5))
        ));
        AbyssDifficultyConfig nonPositive = new AbyssDifficultyConfig(List.of(
                tier(0, null, weights("EASY", 1.0, "HARD", 0.0))
        ));
        AbyssDifficultyConfig unknown = new AbyssDifficultyConfig(List.of(
                tier(0, null, weights("IMPOSSIBLE", 1.0))
        ));

        assertThrows(IllegalStateException.class, wrongSum::validate);
        assertThrows(IllegalStateException.class, nonPositive::validate);
        assertThrows(IllegalStateException.class, unknown::validate);
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
