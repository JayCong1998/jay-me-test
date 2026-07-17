package com.jaymetest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaymetest.model.entity.AlbumProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 专辑进度 Mapper
 */
@Mapper
public interface AlbumProgressMapper extends BaseMapper<AlbumProgress> {

    /** 查询用户所有专辑进度 */
    @Select("SELECT * FROM album_progress WHERE user_id = #{userId} ORDER BY id")
    List<AlbumProgress> selectByUserId(@Param("userId") Long userId);

    /** 查询用户某张专辑的进度 */
    @Select("SELECT * FROM album_progress WHERE user_id = #{userId} AND album_key = #{albumKey}")
    AlbumProgress selectByUserAndAlbum(@Param("userId") Long userId, @Param("albumKey") String albumKey);
}
