package com.jaymetest.service.game.cache;

import com.jaymetest.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameRoundCacheTest {

    @Test
    void recordsAnswersAndRejectsDuplicateAnswerForOneQuestion() {
        GameRoundCache cache = new GameRoundCache(Map.of(1L, "A", 2L, "B"));

        cache.recordAnswer(1L, true);
        cache.recordAnswer(2L, false);

        assertEquals(1, cache.getCorrectCount());
        assertEquals(2, cache.getAnsweredCount());
        assertThrows(BusinessException.class, () -> cache.recordAnswer(1L, true));
    }
}
