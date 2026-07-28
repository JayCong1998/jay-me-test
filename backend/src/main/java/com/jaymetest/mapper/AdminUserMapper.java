package com.jaymetest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaymetest.model.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
