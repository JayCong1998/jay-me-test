package com.jaymetest.service.game.strategy;

import com.jaymetest.config.AbyssGameProperties;
import com.jaymetest.config.AlbumGameProperties;
import com.jaymetest.config.ClassicGameProperties;
import com.jaymetest.mapper.QuestionMapper;
import com.jaymetest.service.game.abyss.AbyssDifficultyPolicy;
import com.jaymetest.service.game.hook.AlbumUnlockHook;
import com.jaymetest.service.game.strategy.impl.AbyssGameStrategy;
import com.jaymetest.service.game.strategy.impl.AlbumGameStrategy;
import com.jaymetest.service.game.strategy.impl.ClassicGameStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class GameStrategyInterfaceSplitTest {

    @Test
    void fixedAndBatchRoundCapabilitiesAreDeclaredSeparately() {
        QuestionMapper questionMapper = mock(QuestionMapper.class);
        ClassicGameStrategy classic = new ClassicGameStrategy(questionMapper, new ClassicGameProperties());
        AlbumGameStrategy album = new AlbumGameStrategy(questionMapper, mock(AlbumUnlockHook.class), new AlbumGameProperties());
        AbyssGameStrategy abyss = new AbyssGameStrategy(
                questionMapper,
                mock(AbyssDifficultyPolicy.class),
                new AbyssGameProperties());

        assertInstanceOf(FixedRoundStrategy.class, classic);
        assertInstanceOf(FixedRoundStrategy.class, album);
        assertInstanceOf(BatchRoundStrategy.class, abyss);
        assertFalse(abyss instanceof FixedRoundStrategy);
    }
}
