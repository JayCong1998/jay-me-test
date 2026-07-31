package com.jaymetest.service.game;

import com.jaymetest.model.dto.GameRecordDTO;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.model.enums.GameMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * 将持久化记录转换为带完整模式语义的接口 DTO。
 */
@Component
@RequiredArgsConstructor
public class GameRecordDTOAssembler {

    private static final DateTimeFormatter CREATED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final GameStrategyFactory strategyFactory;

    public GameRecordDTO toDTO(GameRecord record) {
        GameMode mode = parseMode(record.getMode());
        validateAlbumKey(mode, record.getAlbumKey());

        GameStrategy strategy = strategyFactory.get(mode);
        LevelInfo level = strategy.evaluateLevel(record.getCorrectCount());

        return GameRecordDTO.builder()
                .roundId(record.getRoundId())
                .mode(mode)
                .albumKey(record.getAlbumKey())
                .score(record.getScore())
                .correctCount(record.getCorrectCount())
                .totalQuestions(record.getTotalQuestions())
                .timeSpentSecs(record.getTimeSpentSecs())
                .usedRevival(Integer.valueOf(1).equals(record.getUsedRevival()))
                .createdAt(record.getCreatedAt() == null
                        ? null
                        : record.getCreatedAt().format(CREATED_AT_FORMATTER))
                .level(level.name())
                .levelTitle(level.getTitle())
                .build();
    }

    private GameMode parseMode(String storedMode) {
        try {
            return GameMode.valueOf(storedMode);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalStateException("游戏记录包含非法模式: " + storedMode, exception);
        }
    }

    private void validateAlbumKey(GameMode mode, String albumKey) {
        boolean hasAlbumKey = albumKey != null && !albumKey.isBlank();
        if ((mode == GameMode.ALBUM) != hasAlbumKey) {
            throw new IllegalStateException(
                    "游戏记录的 mode 与 albumKey 不匹配: mode=" + mode + ", albumKey=" + albumKey);
        }
    }
}
