package com.jaymetest.service.game.cache;

import com.jaymetest.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundCacheManagerTest {

    @Test
    void expiresRoundsUsingTheCacheTtl() throws InterruptedException {
        RoundCacheManager manager = new RoundCacheManager(Duration.ofMillis(20), 100);
        GameRoundCache round = new GameRoundCache(Map.of(1L, "A"));

        manager.put("round-1", round);

        assertSame(round, manager.getOrThrow("round-1"));

        Thread.sleep(40);

        assertThrows(BusinessException.class, () -> manager.getOrThrow("round-1"));
    }

    @Test
    void doesNotExposeAScheduledCleanupMethod() {
        assertTrue(java.util.Arrays.stream(RoundCacheManager.class.getDeclaredMethods())
                .noneMatch(method -> method.getName().equals("cleanExpiredCache")));
    }
}
