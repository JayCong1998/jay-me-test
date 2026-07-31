package com.jaymetest.mapper;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LeaderboardRankingSqlTest {

    private final String mapperSource = readMapperSource();

    @Test
    void classicLeaderboardRanksLoggedInClassicRecordsWithCreatedAtTieBreak() {
        assertTrue(mapperSource.contains("gr.mode = #{classicMode}"));
        assertTrue(mapperSource.contains("gr.user_id IS NOT NULL"));
        assertTrue(mapperSource.contains("ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC, gr.created_at ASC"));
        assertTrue(mapperSource.contains("selectClassicLeaderboardPaged"));
        assertTrue(mapperSource.contains("selectMyClassicRank"));
    }

    @Test
    void keepsClassicRankingByCorrectCountAndRemovesUnusedLeaderboardQueries() {
        assertTrue(mapperSource.contains("ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC"));
        assertFalse(mapperSource.contains("selectTotalLeaderboard"));
        assertFalse(mapperSource.contains("selectDailyLeaderboard"));
        assertFalse(mapperSource.contains("selectLevelLeaderboard"));
        assertFalse(mapperSource.contains("getTotalRank"));
        assertFalse(mapperSource.contains("getDailyRank"));
        assertFalse(mapperSource.contains("getLevelRank"));
        assertFalse(mapperSource.contains("selectAbyssLeaderboard("));
    }

    @Test
    void albumLeaderboardRanksAlbumParticipantsAndCountsPassedAlbums() {
        assertTrue(mapperSource.contains("gr.mode = #{albumMode}"));
        assertTrue(mapperSource.contains("PARTITION BY gr.user_id, gr.album_key"));
        assertTrue(mapperSource.contains("SUM(CASE WHEN picked.correct_count * 100 >= picked.total_questions * #{passAccuracy} THEN 1 ELSE 0 END) AS completedAlbumCount"));
        assertTrue(mapperSource.contains("SUM(time_spent_secs) AS totalAlbumTimeSecs"));
        assertTrue(mapperSource.contains("ORDER BY completed_album_count DESC, total_album_score DESC"));
        assertTrue(mapperSource.contains("selectMyAlbumRank"));
    }

    @Test
    void abyssLeaderboardRanksLoggedInAbyssRecordsWithCreatedAtTieBreak() {
        assertTrue(mapperSource.contains("gr.mode = #{abyssMode}"));
        assertTrue(mapperSource.contains("correct_count AS streak"));
        assertTrue(mapperSource.contains("ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC, gr.created_at ASC"));
        assertTrue(mapperSource.contains("selectAbyssLeaderboardPaged"));
        assertTrue(mapperSource.contains("selectMyAbyssRank"));
    }

    private static String readMapperSource() {
        try {
            return Files.readString(Path.of("src/main/java/com/jaymetest/mapper/GameRecordMapper.java"));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read GameRecordMapper source", e);
        }
    }
}
