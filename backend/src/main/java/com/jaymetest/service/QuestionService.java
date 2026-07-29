package com.jaymetest.service;

import com.jaymetest.model.dto.AnswerResultDTO;
import com.jaymetest.service.game.AbyssGameStrategy;
import com.jaymetest.service.game.AlbumGameStrategy;
import com.jaymetest.service.game.ClassicGameStrategy;
import com.jaymetest.service.game.GameRoundCache;
import com.jaymetest.service.game.GameStrategyFactory;
import com.jaymetest.service.game.RoundCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 题目服务对外提供模式策略的统一入口；作答必须由对应策略记录到服务端 Round 缓存。
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final GameStrategyFactory strategyFactory;
    private final RoundCacheManager cacheManager;
    private final ClassicGameStrategy classicStrategy;
    private final AlbumGameStrategy albumStrategy;
    private final AbyssGameStrategy abyssStrategy;

    public AnswerResultDTO checkAnswer(String roundId, Long questionId, String selectedOption) {
        GameRoundCache cache = cacheManager.getOrThrow(roundId);
        return strategyFactory.get(cache.getMode())
                .checkAnswer(roundId, questionId, selectedOption, cacheManager);
    }

    public ClassicGameStrategy classic() {
        return classicStrategy;
    }

    public AlbumGameStrategy album() {
        return albumStrategy;
    }

    public AbyssGameStrategy abyss() {
        return abyssStrategy;
    }

    public RoundCacheManager cacheManager() {
        return cacheManager;
    }
}
