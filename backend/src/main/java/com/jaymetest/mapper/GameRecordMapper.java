package com.jaymetest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaymetest.model.entity.GameRecord;
import com.jaymetest.model.enums.AlbumKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 游戏记录 Mapper
 */
@Mapper
public interface GameRecordMapper extends BaseMapper<GameRecord> {

    /**
     * 统计得分低于指定分数的记录数
     */
    @Select("SELECT COUNT(*) FROM game_record WHERE correct_count < #{score}")
    long countByCorrectCountLessThan(@Param("score") int score);

    /**
     * 统计总记录数
     */
    @Select("SELECT COUNT(*) FROM game_record")
    long countTotal();

    /**
     * 计算平均分
     */
    @Select("SELECT COALESCE(AVG(correct_count * 10.0), 0) FROM game_record")
    double selectAverageScore();

    @Select("SELECT COALESCE(AVG(correct_count), 0) FROM game_record")
    double selectAverageCorrectCount();

    @Select("SELECT COUNT(*) FROM game_record WHERE DATE(created_at) = CURDATE()")
    long countToday();

    @Select("SELECT mode, COUNT(*) as cnt FROM game_record GROUP BY mode ORDER BY cnt DESC")
    List<Map<String, Object>> selectModeDistribution();

    /**
     * 统计各等级分布
     */
    @Select("SELECT correct_count, COUNT(*) as cnt FROM game_record GROUP BY correct_count")
    List<Map<String, Object>> selectLevelDistribution();

    /**
     * 总分榜：每个用户取最佳成绩排名（含游客，游客每条记录独立排名）
     */
    @Select("SELECT t.`rank`, t.nickname, t.correct_count AS correctCount, " +
            "t.time_spent_secs AS timeSpentSecs " +
            "FROM (SELECT gr.nickname, gr.correct_count, gr.time_spent_secs, " +
            "ROW_NUMBER() OVER (ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS `rank`, " +
            "ROW_NUMBER() OVER (PARTITION BY CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END " +
            "ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS rn " +
            "FROM game_record gr) t " +
            "WHERE t.rn = 1 ORDER BY t.`rank` LIMIT #{limit}")
    List<Map<String, Object>> selectTotalLeaderboard(@Param("limit") int limit);

    /**
     * 每日榜：每个用户取今天最佳成绩排名（含游客）
     */
    @Select("SELECT t.`rank`, t.nickname, t.correct_count AS correctCount, " +
            "t.time_spent_secs AS timeSpentSecs " +
            "FROM (SELECT gr.nickname, gr.correct_count, gr.time_spent_secs, " +
            "ROW_NUMBER() OVER (ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS `rank`, " +
            "ROW_NUMBER() OVER (PARTITION BY CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END " +
            "ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS rn " +
            "FROM game_record gr " +
            "WHERE DATE(gr.created_at) = CURDATE()) t " +
            "WHERE t.rn = 1 ORDER BY t.`rank` LIMIT #{limit}")
    List<Map<String, Object>> selectDailyLeaderboard(@Param("limit") int limit);

    /**
     * 等级分榜：指定等级内每个用户取最佳成绩排名（含游客）
     */
    @Select("SELECT t.`rank`, t.nickname, t.correct_count AS correctCount, " +
            "t.time_spent_secs AS timeSpentSecs " +
            "FROM (SELECT gr.nickname, gr.correct_count, gr.time_spent_secs, " +
            "ROW_NUMBER() OVER (ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS `rank`, " +
            "ROW_NUMBER() OVER (PARTITION BY CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END " +
            "ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS rn " +
            "FROM game_record gr " +
            "WHERE gr.correct_count >= #{minScore} AND gr.correct_count <= #{maxScore}) t " +
            "WHERE t.rn = 1 ORDER BY t.`rank` LIMIT #{limit}")
    List<Map<String, Object>> selectLevelLeaderboard(@Param("minScore") int minScore,
                                                     @Param("maxScore") int maxScore,
                                                     @Param("limit") int limit);

    /**
     * 获取总分榜上指定用户的排名（含游客，同分取最佳+最快）
     */
    @Select("SELECT COUNT(*) + 1 FROM " +
            "(SELECT CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END AS uid, " +
            "MAX(gr.correct_count) AS best, MIN(gr.time_spent_secs) AS fastest " +
            "FROM game_record gr GROUP BY uid) u " +
            "WHERE u.best > #{correctCount} " +
            "OR (u.best = #{correctCount} AND u.fastest < #{timeSpentSecs})")
    long getTotalRank(@Param("correctCount") int correctCount,
                      @Param("timeSpentSecs") int timeSpentSecs);

    /**
     * 获取每日榜上指定用户的排名（含游客）
     */
    @Select("SELECT COUNT(*) + 1 FROM " +
            "(SELECT CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END AS uid, " +
            "MAX(gr.correct_count) AS best, MIN(gr.time_spent_secs) AS fastest " +
            "FROM game_record gr " +
            "WHERE DATE(gr.created_at) = CURDATE() GROUP BY uid) u " +
            "WHERE u.best > #{correctCount} " +
            "OR (u.best = #{correctCount} AND u.fastest < #{timeSpentSecs})")
    long getDailyRank(@Param("correctCount") int correctCount,
                      @Param("timeSpentSecs") int timeSpentSecs);

    /**
     * 获取等级榜上指定用户的排名（含游客）
     */
    @Select("SELECT COUNT(*) + 1 FROM " +
            "(SELECT CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END AS uid, " +
            "MAX(gr.correct_count) AS best, MIN(gr.time_spent_secs) AS fastest " +
            "FROM game_record gr " +
            "WHERE gr.correct_count >= #{minScore} AND gr.correct_count <= #{maxScore} " +
            "GROUP BY uid) u " +
            "WHERE u.best > #{correctCount} " +
            "OR (u.best = #{correctCount} AND u.fastest < #{timeSpentSecs})")
    long getLevelRank(@Param("correctCount") int correctCount,
                      @Param("timeSpentSecs") int timeSpentSecs,
                      @Param("minScore") int minScore,
                      @Param("maxScore") int maxScore);

    /**
     * 深渊排行榜：按 streak（correct_count）降序 + 用时升序，每人只取最佳记录
     */
    @Select("SELECT t.`rank`, t.nickname, t.correct_count AS correctCount, " +
            "t.time_spent_secs AS timeSpentSecs " +
            "FROM (SELECT gr.nickname, gr.correct_count, gr.time_spent_secs, " +
            "ROW_NUMBER() OVER (ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS `rank`, " +
            "ROW_NUMBER() OVER (PARTITION BY CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END " +
            "ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS rn " +
            "FROM game_record gr WHERE gr.mode = #{abyssMode}) t " +
            "WHERE t.rn = 1 ORDER BY t.`rank` LIMIT #{limit}")
    List<Map<String, Object>> selectAbyssLeaderboard(@Param("limit") int limit, @Param("abyssMode") String abyssMode);

    /**
     * 获取深渊榜上当前用户的排名
     */
    @Select("SELECT COUNT(*) + 1 FROM " +
            "(SELECT CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END AS uid, " +
            "MAX(gr.correct_count) AS best, MIN(gr.time_spent_secs) AS fastest " +
            "FROM game_record gr WHERE gr.mode = #{abyssMode} GROUP BY uid) u " +
            "WHERE u.best > #{correctCount} " +
            "OR (u.best = #{correctCount} AND u.fastest < #{timeSpentSecs})")
    long getAbyssRank(@Param("correctCount") int correctCount,
                      @Param("timeSpentSecs") int timeSpentSecs,
                      @Param("abyssMode") String abyssMode);

    @Select("""
            SELECT ranked.`rank`, ranked.nickname, ranked.correct_count AS correctCount,
                   ranked.time_spent_secs AS timeSpentSecs, ranked.created_at AS createdAt
            FROM (
                SELECT best.*,
                       ROW_NUMBER() OVER (
                           ORDER BY best.correct_count DESC, best.time_spent_secs ASC, best.created_at ASC, best.user_id ASC
                       ) AS `rank`
                FROM (
                    SELECT gr.user_id, gr.nickname, gr.correct_count, gr.time_spent_secs, gr.created_at,
                           ROW_NUMBER() OVER (
                               PARTITION BY gr.user_id
                               ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC, gr.created_at ASC, gr.id ASC
                           ) AS rn
                    FROM game_record gr
                    WHERE gr.mode = #{classicMode}
                      AND gr.user_id IS NOT NULL
                ) best
                WHERE best.rn = 1
            ) ranked
            ORDER BY ranked.`rank`
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Map<String, Object>> selectClassicLeaderboardPaged(@Param("limit") int limit,
                                                            @Param("offset") int offset,
                                                            @Param("classicMode") String classicMode);

    /**
     * 经典榜只统计登录用户，并先在用户维度取最佳记录。
     *
     * <p>排序稳定性很重要：同分同用时再按创建时间和 user_id 排，避免翻页时排名抖动。</p>
     */
    @Select("""
            SELECT ranked.`rank`
            FROM (
                SELECT best.user_id,
                       ROW_NUMBER() OVER (
                           ORDER BY best.correct_count DESC, best.time_spent_secs ASC, best.created_at ASC, best.user_id ASC
                       ) AS `rank`
                FROM (
                    SELECT gr.user_id, gr.correct_count, gr.time_spent_secs, gr.created_at,
                           ROW_NUMBER() OVER (
                               PARTITION BY gr.user_id
                               ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC, gr.created_at ASC, gr.id ASC
                           ) AS rn
                    FROM game_record gr
                    WHERE gr.mode = #{classicMode}
                      AND gr.user_id IS NOT NULL
                ) best
                WHERE best.rn = 1
            ) ranked
            WHERE ranked.user_id = #{userId}
            """)
    Long selectMyClassicRank(@Param("userId") long userId, @Param("classicMode") String classicMode);

    /**
     * 专辑榜统计“已通关专辑数”，每个用户每张专辑只取一次最佳通关记录。
     *
     * <p>completionScore 来自 {@link AlbumKey#UNLOCK_THRESHOLD}，保持排行榜口径和解锁规则一致。</p>
     */
    @Select("""
            SELECT ranked.`rank`, ranked.nickname,
                   ranked.completedAlbumCount AS completedAlbumCount,
                   ranked.totalAlbumTimeSecs AS totalAlbumTimeSecs,
                   ranked.totalAlbumTimeSecs AS timeSpentSecs,
                   ranked.bestAlbumKey AS bestAlbumKey,
                   ranked.created_at AS createdAt
            FROM (
                SELECT agg.*,
                       ROW_NUMBER() OVER (
                           ORDER BY completed_album_count DESC, total_album_time_secs ASC, created_at ASC, user_id ASC
                       ) AS `rank`
                FROM (
                    SELECT picked.user_id,
                           SUBSTRING_INDEX(GROUP_CONCAT(picked.nickname ORDER BY picked.created_at DESC, picked.id DESC), ',', 1) AS nickname,
                           COUNT(*) AS completedAlbumCount,
                           COUNT(*) AS completed_album_count,
                           SUM(time_spent_secs) AS totalAlbumTimeSecs,
                           SUM(time_spent_secs) AS total_album_time_secs,
                           SUBSTRING_INDEX(GROUP_CONCAT(picked.album_key ORDER BY picked.created_at DESC, picked.id DESC), ',', 1) AS bestAlbumKey,
                           MAX(picked.created_at) AS created_at
                    FROM (
                        SELECT album_best.*
                        FROM (
                            SELECT gr.id, gr.user_id, gr.nickname, gr.album_key, gr.correct_count,
                                   gr.time_spent_secs, gr.created_at,
                                   ROW_NUMBER() OVER (
                                       PARTITION BY gr.user_id, gr.album_key
                                       ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC, gr.created_at ASC, gr.id ASC
                                   ) AS rn
                            FROM game_record gr
                            WHERE gr.mode = #{albumMode}
                              AND gr.user_id IS NOT NULL
                              AND gr.album_key IS NOT NULL
                              AND gr.correct_count >= #{completionScore}
                        ) album_best
                        WHERE album_best.rn = 1
                    ) picked
                    GROUP BY picked.user_id
                ) agg
            ) ranked
            ORDER BY ranked.`rank`
            LIMIT #{limit} OFFSET #{offset}
            """)
            List<Map<String, Object>> selectAlbumLeaderboardPaged(@Param("limit") int limit,
                                                          @Param("offset") int offset,
                                                          @Param("albumMode") String albumMode,
                                                          @Param("completionScore") int completionScore);

    /**
     * 当前用户专辑榜排名和列表 SQL 使用同一套聚合口径，避免“榜上排名”和“我的排名”不一致。
     */
    @Select("""
            SELECT ranked.`rank`
            FROM (
                SELECT agg.user_id,
                       ROW_NUMBER() OVER (
                           ORDER BY completed_album_count DESC, total_album_time_secs ASC, created_at ASC, user_id ASC
                       ) AS `rank`
                FROM (
                    SELECT picked.user_id,
                           COUNT(*) AS completed_album_count,
                           SUM(time_spent_secs) AS total_album_time_secs,
                           MAX(picked.created_at) AS created_at
                    FROM (
                        SELECT album_best.*
                        FROM (
                            SELECT gr.id, gr.user_id, gr.album_key, gr.correct_count,
                                   gr.time_spent_secs, gr.created_at,
                                   ROW_NUMBER() OVER (
                                       PARTITION BY gr.user_id, gr.album_key
                                       ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC, gr.created_at ASC, gr.id ASC
                                   ) AS rn
                            FROM game_record gr
                            WHERE gr.mode = #{albumMode}
                              AND gr.user_id IS NOT NULL
                              AND gr.album_key IS NOT NULL
                              AND gr.correct_count >= #{completionScore}
                        ) album_best
                        WHERE album_best.rn = 1
                    ) picked
                    GROUP BY picked.user_id
                ) agg
            ) ranked
            WHERE ranked.user_id = #{userId}
            """)
    Long selectMyAlbumRank(@Param("userId") long userId,
                           @Param("albumMode") String albumMode,
                           @Param("completionScore") int completionScore);

    /**
     * 深渊榜以连续答对数为主排序，用时只作为同 streak 的 tie-breaker。
     */
    @Select("""
            SELECT ranked.`rank`
            FROM (
                SELECT best.user_id,
                       ROW_NUMBER() OVER (
                           ORDER BY best.correct_count DESC, best.time_spent_secs ASC, best.created_at ASC, best.user_id ASC
                       ) AS `rank`
                FROM (
                    SELECT gr.user_id, gr.correct_count, gr.time_spent_secs, gr.created_at,
                           ROW_NUMBER() OVER (
                               PARTITION BY gr.user_id
                               ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC, gr.created_at ASC, gr.id ASC
                           ) AS rn
                    FROM game_record gr
                    WHERE gr.mode = #{abyssMode}
                      AND gr.user_id IS NOT NULL
                ) best
                WHERE best.rn = 1
            ) ranked
            WHERE ranked.user_id = #{userId}
            """)
    Long selectMyAbyssRank(@Param("userId") long userId, @Param("abyssMode") String abyssMode);

    /**
     * 查询指定用户的考试记录（分页）
     */
    @Select("SELECT * FROM game_record WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<GameRecord> selectByUserId(@Param("userId") long userId, @Param("limit") int limit, @Param("offset") int offset);

    /**
     * 统计指定用户的考试记录总数
     */
    @Select("SELECT COUNT(*) FROM game_record WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") long userId);

    /**
     * 总分榜（分页）
     */
    @Select("SELECT t.`rank`, t.nickname, t.correct_count AS correctCount, " +
            "t.time_spent_secs AS timeSpentSecs " +
            "FROM (SELECT gr.nickname, gr.correct_count, gr.time_spent_secs, " +
            "ROW_NUMBER() OVER (ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS `rank`, " +
            "ROW_NUMBER() OVER (PARTITION BY CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END " +
            "ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS rn " +
            "FROM game_record gr) t " +
            "WHERE t.rn = 1 ORDER BY t.`rank` LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> selectTotalLeaderboardPaged(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 每日榜（分页）
     */
    @Select("SELECT t.`rank`, t.nickname, t.correct_count AS correctCount, " +
            "t.time_spent_secs AS timeSpentSecs " +
            "FROM (SELECT gr.nickname, gr.correct_count, gr.time_spent_secs, " +
            "ROW_NUMBER() OVER (ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS `rank`, " +
            "ROW_NUMBER() OVER (PARTITION BY CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END " +
            "ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS rn " +
            "FROM game_record gr " +
            "WHERE DATE(gr.created_at) = CURDATE()) t " +
            "WHERE t.rn = 1 ORDER BY t.`rank` LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> selectDailyLeaderboardPaged(@Param("limit") int limit, @Param("offset") int offset);

    /**
     * 等级分榜（分页）
     */
    @Select("SELECT t.`rank`, t.nickname, t.correct_count AS correctCount, " +
            "t.time_spent_secs AS timeSpentSecs " +
            "FROM (SELECT gr.nickname, gr.correct_count, gr.time_spent_secs, " +
            "ROW_NUMBER() OVER (ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS `rank`, " +
            "ROW_NUMBER() OVER (PARTITION BY CASE WHEN gr.user_id IS NOT NULL THEN gr.user_id ELSE -gr.id END " +
            "ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC) AS rn " +
            "FROM game_record gr " +
            "WHERE gr.correct_count >= #{minScore} AND gr.correct_count <= #{maxScore}) t " +
            "WHERE t.rn = 1 ORDER BY t.`rank` LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> selectLevelLeaderboardPaged(@Param("minScore") int minScore,
                                                          @Param("maxScore") int maxScore,
                                                          @Param("limit") int limit,
                                                          @Param("offset") int offset);

    /**
     * 深渊排行榜（分页）
     */
    @Select("""
            SELECT ranked.`rank`, ranked.nickname, ranked.correct_count AS correctCount,
                   ranked.correct_count AS streak,
                   ranked.time_spent_secs AS timeSpentSecs, ranked.created_at AS createdAt
            FROM (
                SELECT best.*,
                       ROW_NUMBER() OVER (
                           ORDER BY best.correct_count DESC, best.time_spent_secs ASC, best.created_at ASC, best.user_id ASC
                       ) AS `rank`
                FROM (
                    SELECT gr.user_id, gr.nickname, gr.correct_count, gr.time_spent_secs, gr.created_at,
                           ROW_NUMBER() OVER (
                               PARTITION BY gr.user_id
                               ORDER BY gr.correct_count DESC, gr.time_spent_secs ASC, gr.created_at ASC, gr.id ASC
                           ) AS rn
                    FROM game_record gr
                    WHERE gr.mode = #{abyssMode}
                      AND gr.user_id IS NOT NULL
                ) best
                WHERE best.rn = 1
            ) ranked
            ORDER BY ranked.`rank`
            LIMIT #{limit} OFFSET #{offset}
            """)
    List<Map<String, Object>> selectAbyssLeaderboardPaged(@Param("limit") int limit,
                                                           @Param("offset") int offset,
                                                           @Param("abyssMode") String abyssMode);
}
