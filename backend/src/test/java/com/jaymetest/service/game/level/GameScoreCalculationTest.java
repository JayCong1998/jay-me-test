package com.jaymetest.service.game.level;

import com.jaymetest.config.AlbumGameProperties;
import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.service.game.strategy.impl.AlbumGameStrategy;
import com.jaymetest.service.game.strategy.impl.ClassicGameStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameScoreCalculationTest {

    @Test
    void classicScoreUsesTheActualQuestionCountAsThePercentageDenominator() {
        ClassicGameStrategy strategy = new ClassicGameStrategy(null, new ClassicGameProperties());

        assertEquals(10, strategy.calculateScore(1, 10));
        assertEquals(5, strategy.calculateScore(1, 20));
    }

    @Test
    void albumScoreUsesTheActualQuestionCountAsThePercentageDenominator() {
        AlbumGameStrategy strategy = new AlbumGameStrategy(null, null, new AlbumGameProperties());

        assertEquals(10, strategy.calculateScore(1, 10));
        assertEquals(5, strategy.calculateScore(1, 20));
    }
}
