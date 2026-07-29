package com.jaymetest.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassicGamePropertiesTest {

    @Test
    void rejectsOverlappingLevelRanges() {
        ClassicGameProperties properties = validProperties();
        properties.setLevels(List.of(
                level("PASSERBY", 0, 50),
                level("JUNIOR", 50, 100)
        ));

        assertThrows(IllegalStateException.class, properties::validate);
    }

    @Test
    void acceptsContiguousAccuracyRangesCoveringZeroToOneHundred() {
        assertDoesNotThrow(validProperties()::validate);
    }

    private ClassicGameProperties validProperties() {
        ClassicGameProperties properties = new ClassicGameProperties();
        properties.setQuestionCount(10);
        properties.setRevivalCount(1);
        properties.setEasyWeight(0.6);
        properties.setLevels(List.of(
                level("PASSERBY", 0, 29),
                level("JUNIOR", 30, 100)
        ));
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
