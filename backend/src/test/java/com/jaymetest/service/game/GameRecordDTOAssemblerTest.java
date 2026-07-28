package com.jaymetest.service.game;

import com.jaymetest.model.dto.GameRecordDTO;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.model.enums.AbyssLevel;
import com.jaymetest.model.enums.FanLevel;
import com.jaymetest.model.enums.GameMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameRecordDTOAssemblerTest {

    @Mock
    private GameStrategyFactory strategyFactory;

    @Mock
    private GameStrategy strategy;

    private GameRecordDTOAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new GameRecordDTOAssembler(strategyFactory);
    }

    @Test
    void convertsClassicRecordWithServerCalculatedScoreAndLevel() {
        GameRecord record = record(GameMode.CLASSIC, null, 8, 10);
        when(strategyFactory.get(GameMode.CLASSIC)).thenReturn(strategy);
        when(strategy.calculateScore(8, 10)).thenReturn(80);
        when(strategy.evaluateLevel(8)).thenReturn(FanLevel.SENIOR);

        GameRecordDTO result = assembler.toDTO(record);

        assertEquals("round-1", result.getRoundId());
        assertEquals(GameMode.CLASSIC, result.getMode());
        assertNull(result.getAlbumKey());
        assertEquals(80, result.getScore());
        assertEquals(FanLevel.SENIOR.name(), result.getLevel());
        assertEquals(FanLevel.SENIOR.getTitle(), result.getLevelTitle());
        assertEquals("2026-07-20T18:00:00", result.getCreatedAt());
    }

    @Test
    void convertsAbyssRecordWithoutTenQuestionScoreFallback() {
        GameRecord record = record(GameMode.ABYSS, null, 12, 13);
        when(strategyFactory.get(GameMode.ABYSS)).thenReturn(strategy);
        when(strategy.calculateScore(12, 13)).thenReturn(12);
        when(strategy.evaluateLevel(12)).thenReturn(AbyssLevel.ABYSS_KNIGHT);

        GameRecordDTO result = assembler.toDTO(record);

        assertEquals(GameMode.ABYSS, result.getMode());
        assertEquals(12, result.getScore());
        assertEquals(AbyssLevel.ABYSS_KNIGHT.name(), result.getLevel());
        assertEquals(AbyssLevel.ABYSS_KNIGHT.getTitle(), result.getLevelTitle());
    }

    @Test
    void rejectsUnknownStoredMode() {
        GameRecord record = record(GameMode.CLASSIC, null, 8, 10);
        record.setMode("UNKNOWN");

        assertThrows(IllegalStateException.class, () -> assembler.toDTO(record));
    }

    @Test
    void rejectsAlbumKeyOnNonAlbumRecord() {
        GameRecord record = record(GameMode.CLASSIC, "叶惠美", 8, 10);

        assertThrows(IllegalStateException.class, () -> assembler.toDTO(record));
    }

    @Test
    void rejectsMissingAlbumKeyOnAlbumRecord() {
        GameRecord record = record(GameMode.ALBUM, null, 8, 10);

        assertThrows(IllegalStateException.class, () -> assembler.toDTO(record));
    }

    private GameRecord record(GameMode mode, String albumKey, int correctCount, int totalQuestions) {
        GameRecord record = new GameRecord();
        record.setRoundId("round-1");
        record.setMode(mode.name());
        record.setAlbumKey(albumKey);
        record.setCorrectCount(correctCount);
        record.setTotalQuestions(totalQuestions);
        record.setTimeSpentSecs(90);
        record.setUsedRevival(0);
        record.setCreatedAt(LocalDateTime.of(2026, 7, 20, 18, 0));
        return record;
    }
}
