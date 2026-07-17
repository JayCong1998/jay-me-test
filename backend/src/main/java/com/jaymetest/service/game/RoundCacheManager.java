package com.jaymetest.service.game;

import com.jaymetest.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Round 缓存管理器 — 所有游戏模式共享的缓存存储。
 * 实现 {@link GameStrategy.RoundCacheManager}，策略通过此接口操作缓存。
 */
@Slf4j
@Component
public class RoundCacheManager implements GameStrategy.RoundCacheManager {

    private final ConcurrentHashMap<String, GameRoundCache> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String roundId, GameRoundCache roundCache) {
        cache.put(roundId, roundCache);
    }

    @Override
    public GameRoundCache get(String roundId) {
        return cache.get(roundId);
    }

    @Override
    public void remove(String roundId) {
        cache.remove(roundId);
    }

    @Override
    public GameRoundCache getOrThrow(String roundId) {
        GameRoundCache c = cache.get(roundId);
        if (c == null) {
            throw new BusinessException(404, "回合不存在或已过期");
        }
        if (c.isExpired()) {
            cache.remove(roundId);
            throw new BusinessException(404, "回合已过期（超过30分钟），请重新开始");
        }
        return c;
    }

    /** 定时清理过期缓存 */
    @Scheduled(fixedRate = 600_000) // 每 10 分钟
    public void cleanExpiredCache() {
        int before = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int after = cache.size();
        if (before != after) {
            log.info("清理过期 Round 缓存: {} → {}", before, after);
        }
    }
}
