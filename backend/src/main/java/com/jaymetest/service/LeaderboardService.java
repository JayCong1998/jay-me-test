package com.jaymetest.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.model.dto.LeaderboardEntry;
import com.jaymetest.model.dto.LeaderboardResult;
import com.jaymetest.model.enums.AlbumKey;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.GameStrategyFactory;
import com.jaymetest.service.game.LevelInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final GameRecordMapper gameRecordMapper;
    private final GameStrategyFactory strategyFactory;

    public LeaderboardResult getClassicLeaderboard(int limit, int offset) {
        List<Map<String, Object>> rows = gameRecordMapper.selectClassicLeaderboardPaged(
                limit, offset, GameMode.CLASSIC.name());
        List<LeaderboardEntry> list = mapClassicEntries(rows);
        Long myRank = gameRecordMapper.selectMyClassicRank(StpUtil.getLoginIdAsLong(), GameMode.CLASSIC.name());
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    public LeaderboardResult getAlbumLeaderboard(int limit, int offset) {
        List<Map<String, Object>> rows = gameRecordMapper.selectAlbumLeaderboardPaged(
                limit, offset, GameMode.ALBUM.name(), AlbumKey.UNLOCK_THRESHOLD);
        List<LeaderboardEntry> list = mapAlbumEntries(rows);
        Long myRank = gameRecordMapper.selectMyAlbumRank(
                StpUtil.getLoginIdAsLong(), GameMode.ALBUM.name(), AlbumKey.UNLOCK_THRESHOLD);
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    public LeaderboardResult getAbyssLeaderboard(int limit) {
        return getAbyssLeaderboard(limit, 0);
    }

    public LeaderboardResult getAbyssLeaderboard(int limit, int offset) {
        List<Map<String, Object>> rows = gameRecordMapper.selectAbyssLeaderboardPaged(
                limit, offset, GameMode.ABYSS.name());
        List<LeaderboardEntry> list = mapAbyssEntries(rows);
        Long myRank = gameRecordMapper.selectMyAbyssRank(StpUtil.getLoginIdAsLong(), GameMode.ABYSS.name());
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    public LeaderboardResult getTotalLeaderboard(int limit) {
        return getClassicLeaderboard(limit, 0);
    }

    public LeaderboardResult getTotalLeaderboard(int limit, int offset) {
        return getClassicLeaderboard(limit, offset);
    }

    public LeaderboardResult getDailyLeaderboard(int limit) {
        return getClassicLeaderboard(limit, 0);
    }

    public LeaderboardResult getDailyLeaderboard(int limit, int offset) {
        return getClassicLeaderboard(limit, offset);
    }

    public LeaderboardResult getLevelLeaderboard(String level, int limit) {
        return getClassicLeaderboard(limit, 0);
    }

    public LeaderboardResult getLevelLeaderboard(String level, int limit, int offset) {
        return getClassicLeaderboard(limit, offset);
    }

    private List<LeaderboardEntry> mapClassicEntries(List<Map<String, Object>> rows) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int correctCount = intValue(row, "correctCount");
            int timeSpentSecs = intValue(row, "timeSpentSecs");
            LevelInfo levelInfo = strategyFactory.get(GameMode.CLASSIC).evaluateLevel(correctCount);
            entries.add(LeaderboardEntry.builder()
                    .rank(longValue(row, "rank"))
                    .nickname((String) row.get("nickname"))
                    .correctCount(correctCount)
                    .timeSpentSecs(timeSpentSecs)
                    .levelTitle(levelInfo.getTitle())
                    .createdAt(dateTimeValue(row, "createdAt"))
                    .scoreText(correctCount + "/10")
                    .summaryText(correctCount + "/10")
                    .detailText(levelInfo.getTitle() + " | Time " + formatDuration(timeSpentSecs))
                    .build());
        }
        return entries;
    }

    private List<LeaderboardEntry> mapAlbumEntries(List<Map<String, Object>> rows) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int completedAlbumCount = intValue(row, "completedAlbumCount");
            int totalAlbumTimeSecs = intValue(row, "totalAlbumTimeSecs");
            String bestAlbumKey = (String) row.get("bestAlbumKey");
            String bestAlbumName = resolveAlbumName(bestAlbumKey);
            entries.add(LeaderboardEntry.builder()
                    .rank(longValue(row, "rank"))
                    .nickname((String) row.get("nickname"))
                    .timeSpentSecs(totalAlbumTimeSecs)
                    .createdAt(dateTimeValue(row, "createdAt"))
                    .completedAlbumCount(completedAlbumCount)
                    .totalAlbumTimeSecs(totalAlbumTimeSecs)
                    .bestAlbumKey(bestAlbumKey)
                    .bestAlbumName(bestAlbumName)
                    .scoreText("Completed " + completedAlbumCount + "/" + AlbumKey.values().length)
                    .summaryText("Completed " + completedAlbumCount + "/" + AlbumKey.values().length)
                    .detailText("Total time " + formatDuration(totalAlbumTimeSecs) + " | Latest " + bestAlbumName)
                    .build());
        }
        return entries;
    }

    private List<LeaderboardEntry> mapAbyssEntries(List<Map<String, Object>> rows) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int streak = intValue(row, "streak");
            int timeSpentSecs = intValue(row, "timeSpentSecs");
            LevelInfo levelInfo = strategyFactory.get(GameMode.ABYSS).evaluateLevel(streak);
            entries.add(LeaderboardEntry.builder()
                    .rank(longValue(row, "rank"))
                    .nickname((String) row.get("nickname"))
                    .correctCount(streak)
                    .streak(streak)
                    .timeSpentSecs(timeSpentSecs)
                    .levelTitle(levelInfo.getTitle())
                    .createdAt(dateTimeValue(row, "createdAt"))
                    .scoreText(streak + " streak")
                    .summaryText(streak + " streak")
                    .detailText(levelInfo.getTitle() + " | Time " + formatDuration(timeSpentSecs))
                    .build());
        }
        return entries;
    }

    private int intValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? 0 : ((Number) value).intValue();
    }

    private long longValue(Map<String, Object> row, String key) {
        return ((Number) row.get(key)).longValue();
    }

    private LocalDateTime dateTimeValue(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private String resolveAlbumName(String key) {
        if (key == null || key.isBlank()) {
            return "Unknown album";
        }
        try {
            return AlbumKey.valueOf(key).getDisplayName();
        } catch (IllegalArgumentException ignored) {
            return key;
        }
    }

    private String formatDuration(int secs) {
        int minutes = secs / 60;
        int seconds = secs % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}