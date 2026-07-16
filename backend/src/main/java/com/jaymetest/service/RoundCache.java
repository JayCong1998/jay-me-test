package com.jaymetest.service;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * Round 缓存对象
 */
@Getter
public class RoundCache {
    /** questionId → correctOption */
    private final Map<Long, String> answerMap;
    private final Instant createdAt;

    public RoundCache(Map<Long, String> answerMap) {
        this.answerMap = answerMap;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(createdAt.plusSeconds(30 * 60)); // 30 分钟过期
    }
}
