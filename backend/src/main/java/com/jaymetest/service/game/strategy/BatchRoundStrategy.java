package com.jaymetest.service.game.strategy;

import com.jaymetest.model.dto.AbyssStepDTO;
import com.jaymetest.service.game.cache.RoundCacheManager;

public interface BatchRoundStrategy extends GameStrategy {

    AbyssStepDTO generateStart(RoundCacheManager roundCacheManager);

    AbyssStepDTO generateBatch(String roundId, RoundCacheManager roundCacheManager);
}
