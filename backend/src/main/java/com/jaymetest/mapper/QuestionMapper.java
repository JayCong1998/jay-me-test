package com.jaymetest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaymetest.model.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题目 Mapper
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 按难度随机抽取指定数量的题目
     */
    @Select("SELECT * FROM question WHERE difficulty = #{difficulty} ORDER BY RAND() LIMIT #{limit}")
    List<Question> selectRandomByDifficulty(@Param("difficulty") String difficulty,
                                             @Param("limit") int limit);

    /**
     * 查询题目总数
     */
    @Select("SELECT COUNT(*) FROM question")
    long countTotal();
}
