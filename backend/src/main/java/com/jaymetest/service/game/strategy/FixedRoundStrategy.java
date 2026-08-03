package com.jaymetest.service.game.strategy;

import com.jaymetest.model.dto.RoundDTO;
import com.jaymetest.service.game.cache.RoundCacheManager;

public interface FixedRoundStrategy extends GameStrategy {

    RoundDTO generateRound(String albumKey, RoundCacheManager roundCacheManager);
}
