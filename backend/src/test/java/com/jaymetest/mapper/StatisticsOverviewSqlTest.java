package com.jaymetest.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticsOverviewSqlTest {

    private final String mapperSource = readMapperSource();

    @Test
    void publicOverviewQueriesExcludeGuestRecords() {
        assertTrue(mapperSource.contains("SELECT COUNT(*) FROM game_record WHERE user_id IS NOT NULL"));
        assertTrue(mapperSource.contains("SELECT COALESCE(AVG(score), 0) FROM game_record WHERE user_id IS NOT NULL"));
        assertTrue(mapperSource.contains("WHERE mode = 'ABYSS' AND user_id IS NOT NULL"));
        assertTrue(mapperSource.contains("SELECT correct_count, COUNT(*) as cnt FROM game_record WHERE user_id IS NOT NULL GROUP BY correct_count"));
    }

    private static String readMapperSource() {
        try {
            return Files.readString(Path.of("src/main/java/com/jaymetest/mapper/GameRecordMapper.java"));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read GameRecordMapper source", e);
        }
    }
}
