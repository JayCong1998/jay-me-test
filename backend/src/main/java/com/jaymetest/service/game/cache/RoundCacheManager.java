package com.jaymetest.service.game.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.service.game.strategy.GameStrategy;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Round 缓存管理器，所有玩法共享的本地回合缓存。
 */
@Component
public class RoundCacheManager implements GameStrategy.RoundCacheManager {

    private final Cache<String, GameRoundCache> cache;

    public RoundCacheManager() {
        this(Duration.ofMinutes(30), 10_000);
    }

    public RoundCacheManager(Duration ttl, long maximumSize) {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maximumSize)
                .recordStats()
                .build();
    }

    @Override
    public void put(String roundId, GameRoundCache roundCache) {
        cache.put(roundId, roundCache);
    }

    @Override
    public GameRoundCache get(String roundId) {
        return cache.getIfPresent(roundId);
    }

    @Override
    public void remove(String roundId) {
        cache.invalidate(roundId);
    }

    @Override
    public GameRoundCache getOrThrow(String roundId) {
        GameRoundCache roundCache = cache.getIfPresent(roundId);
        if (roundCache == null) {
            throw new BusinessException(404, "回合不存在或已过期");
        }
        return roundCache;
    }

}
