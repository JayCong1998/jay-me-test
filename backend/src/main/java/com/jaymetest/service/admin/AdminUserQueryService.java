package com.jaymetest.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.admin.PageResponse;
import com.jaymetest.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserQueryService {

    private final UserMapper userMapper;

    public PageResponse<User> list(String keyword, int page, int size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String like = keyword.trim();
            wrapper.and(w -> w.like(User::getEmail, like).or().like(User::getNickname, like));
        }
        wrapper.orderByDesc(User::getId);
        Page<User> result = userMapper.selectPage(new Page<>(Math.max(page, 1), Math.min(Math.max(size, 1), 100)), wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));
        return PageResponse.<User>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .build();
    }
}
