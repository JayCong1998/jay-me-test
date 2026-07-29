package com.jaymetest.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbyssGamePropertiesTest {

    @Test
    void rejectsAnUnreasonablyHighRevivalCount() {
        AbyssGameProperties properties = validProperties();
        properties.setRevivalCount(4);

        assertThrows(IllegalStateException.class, properties::validate);
    }

    @Test
    void acceptsOneAbyssRevivalPerRound() {
        assertDoesNotThrow(validProperties()::validate);
    }

    private AbyssGameProperties validProperties() {
        AbyssGameProperties properties = new AbyssGameProperties();
        properties.setBatchSize(5);
        properties.setRevivalCount(1);
        properties.setLevels(List.of(level("TOURIST", 0, null)));
        return properties;
    }

    private LevelRangeProperties level(String key, int min, Integer max) {
        LevelRangeProperties level = new LevelRangeProperties();
        level.setKey(key);
        level.setTitle(key);
        level.setDescription(key);
        level.setMin(min);
        level.setMax(max);
        return level;
    }
}
