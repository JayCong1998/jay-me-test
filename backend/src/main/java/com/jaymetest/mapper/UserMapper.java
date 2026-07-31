package com.jaymetest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaymetest.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT COUNT(*) FROM user")
    long countTotalUsers();

    default User findByEmail(String email) {
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    default User findByNickname(String nickname) {
        return selectOne(new LambdaQueryWrapper<User>().eq(User::getNickname, nickname));
    }
}
