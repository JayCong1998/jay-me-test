package com.jaymetest.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.dto.LeaderboardEntry;
import com.jaymetest.model.dto.LeaderboardResult;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.model.entity.User;
import com.jaymetest.model.enums.FanLevel;
import com.jaymetest.model.enums.GameMode;
import com.jaymetest.service.game.GameStrategyFactory;
import com.jaymetest.service.game.LevelInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排行榜服务。
 *
 * <p>重构后通过 {@link LevelInfo} 统一 FanLevel 和 AbyssLevel 的等级展示。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final GameRecordMapper gameRecordMapper;
    private final UserMapper userMapper;
    private final GameStrategyFactory strategyFactory;

    /** 总分榜 */
    public LeaderboardResult getTotalLeaderboard(int limit) {
        List<Map<String, Object>> rows = gameRecordMapper.selectTotalLeaderboard(limit);
        List<LeaderboardEntry> list = mapToEntries(rows, GameMode.CLASSIC);
        Long myRank = getMyTotalRank();
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    /** 每日榜 */
    public LeaderboardResult getDailyLeaderboard(int limit) {
        List<Map<String, Object>> rows = gameRecordMapper.selectDailyLeaderboard(limit);
        List<LeaderboardEntry> list = mapToEntries(rows, GameMode.CLASSIC);
        Long myRank = getMyDailyRank();
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    /** 等级分榜 */
    public LeaderboardResult getLevelLeaderboard(String level, int limit) {
        FanLevel fanLevel = FanLevel.valueOf(level.toUpperCase());
        List<Map<String, Object>> rows = gameRecordMapper.selectLevelLeaderboard(
                fanLevel.getMinScore(), fanLevel.getMaxScore(), limit);
        List<LeaderboardEntry> list = mapToEntries(rows, GameMode.CLASSIC);
        Long myRank = getMyLevelRank(fanLevel);
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    /** 深渊排行榜 */
    public LeaderboardResult getAbyssLeaderboard(int limit) {
        List<Map<String, Object>> rows = gameRecordMapper.selectAbyssLeaderboard(limit, GameMode.ABYSS.name());
        List<LeaderboardEntry> list = mapToEntries(rows, GameMode.ABYSS);
        Long myRank = getMyAbyssRank();
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    // ---- 映射工具 ----

    /**
     * 将数据库行映射为排行榜条目，根据模式使用对应的 LevelInfo。
     */
    private List<LeaderboardEntry> mapToEntries(List<Map<String, Object>> rows, GameMode mode) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int correctCount = ((Number) row.get("correctCount")).intValue();
            LevelInfo levelInfo = strategyFactory.get(mode).evaluateLevel(correctCount);
            entries.add(LeaderboardEntry.builder()
                    .rank(((Number) row.get("rank")).longValue())
                    .nickname((String) row.get("nickname"))
                    .correctCount(correctCount)
                    .timeSpentSecs(((Number) row.get("timeSpentSecs")).intValue())
                    .levelTitle(levelInfo.getTitle())
                    .build());
        }
        return entries;
    }

    // ---- 个人排名查询 ----

    private GameRecord getMyBest() {
        long userId = StpUtil.getLoginIdAsLong();
        List<GameRecord> records = gameRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GameRecord>()
                        .eq(GameRecord::getUserId, userId)
                        .orderByDesc(GameRecord::getCorrectCount)
                        .orderByAsc(GameRecord::getTimeSpentSecs)
                        .last("LIMIT 1"));
        return records.isEmpty() ? null : records.get(0);
    }

    private GameRecord getMyAbyssBest() {
        long userId = StpUtil.getLoginIdAsLong();
        List<GameRecord> records = gameRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GameRecord>()
                        .eq(GameRecord::getUserId, userId)
                        .eq(GameRecord::getMode, GameMode.ABYSS.name())
                        .orderByDesc(GameRecord::getCorrectCount)
                        .orderByAsc(GameRecord::getTimeSpentSecs)
                        .last("LIMIT 1"));
        return records.isEmpty() ? null : records.get(0);
    }

    private Long getMyTotalRank() {
        GameRecord best = getMyBest();
        if (best == null) return null;
        return gameRecordMapper.getTotalRank(best.getCorrectCount(), best.getTimeSpentSecs());
    }

    private Long getMyDailyRank() {
        GameRecord best = getMyBest();
        if (best == null) return null;
        return gameRecordMapper.getDailyRank(best.getCorrectCount(), best.getTimeSpentSecs());
    }

    private Long getMyLevelRank(FanLevel fanLevel) {
        GameRecord best = getMyBest();
        if (best == null) return null;
        if (best.getCorrectCount() < fanLevel.getMinScore()
                || best.getCorrectCount() > fanLevel.getMaxScore()) {
            return null;
        }
        return gameRecordMapper.getLevelRank(best.getCorrectCount(), best.getTimeSpentSecs(),
                fanLevel.getMinScore(), fanLevel.getMaxScore());
    }

    private Long getMyAbyssRank() {
        GameRecord best = getMyAbyssBest();
        if (best == null) return null;
        return gameRecordMapper.getAbyssRank(best.getCorrectCount(), best.getTimeSpentSecs(), GameMode.ABYSS.name());
    }
}
