package com.jaymetest.service;

import cn.dev33.satoken.stp.StpUtil;
import com.jaymetest.mapper.GameRecordMapper;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.dto.LeaderboardEntry;
import com.jaymetest.model.dto.LeaderboardResult;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.model.entity.User;
import com.jaymetest.model.enums.FanLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排行榜服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final GameRecordMapper gameRecordMapper;
    private final UserMapper userMapper;

    /**
     * 总分榜
     */
    public LeaderboardResult getTotalLeaderboard(int limit) {
        List<Map<String, Object>> rows = gameRecordMapper.selectTotalLeaderboard(limit);
        List<LeaderboardEntry> list = mapToEntries(rows);
        Long myRank = getMyTotalRank();
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    /**
     * 每日榜
     */
    public LeaderboardResult getDailyLeaderboard(int limit) {
        List<Map<String, Object>> rows = gameRecordMapper.selectDailyLeaderboard(limit);
        List<LeaderboardEntry> list = mapToEntries(rows);
        Long myRank = getMyDailyRank();
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    /**
     * 等级分榜
     */
    public LeaderboardResult getLevelLeaderboard(String level, int limit) {
        FanLevel fanLevel = FanLevel.valueOf(level.toUpperCase());
        List<Map<String, Object>> rows = gameRecordMapper.selectLevelLeaderboard(
                fanLevel.getMinScore(), fanLevel.getMaxScore(), limit);
        List<LeaderboardEntry> list = mapToEntries(rows);
        Long myRank = getMyLevelRank(fanLevel);
        return LeaderboardResult.builder().list(list).myRank(myRank).build();
    }

    /**
     * 获取当前用户的个人最佳成绩
     */
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
        // 只有当前用户的最佳落在指定等级内才返回排名
        if (best.getCorrectCount() < fanLevel.getMinScore()
                || best.getCorrectCount() > fanLevel.getMaxScore()) {
            return null;
        }
        return gameRecordMapper.getLevelRank(best.getCorrectCount(), best.getTimeSpentSecs(),
                fanLevel.getMinScore(), fanLevel.getMaxScore());
    }

    private List<LeaderboardEntry> mapToEntries(List<Map<String, Object>> rows) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int correctCount = ((Number) row.get("correctCount")).intValue();
            FanLevel fanLevel = FanLevel.fromScore(correctCount);
            entries.add(LeaderboardEntry.builder()
                    .rank(((Number) row.get("rank")).longValue())
                    .nickname((String) row.get("nickname"))
                    .correctCount(correctCount)
                    .timeSpentSecs(((Number) row.get("timeSpentSecs")).intValue())
                    .levelTitle(fanLevel.getTitle())
                    .build());
        }
        return entries;
    }
}
