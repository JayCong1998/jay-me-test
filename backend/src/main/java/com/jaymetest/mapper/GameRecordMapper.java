package com.jaymetest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaymetest.model.entity.GameRecord;
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

    /**
     * 查询指定用户的考试记录
     */
    @Select("SELECT * FROM game_record WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<GameRecord> selectByUserId(@Param("userId") long userId, @Param("limit") int limit);
}
