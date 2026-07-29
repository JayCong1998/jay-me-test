package com.jaymetest.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.SaLoginModel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.dto.LoginResponse;
import com.jaymetest.model.dto.UserDTO;
import com.jaymetest.model.dto.UserLoginRequest;
import com.jaymetest.model.dto.UserRegisterRequest;
import com.jaymetest.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 注册
     */
    public LoginResponse register(UserRegisterRequest request) {
        // 检查邮箱是否已注册
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (existing != null) {
            throw new BusinessException(400, "该邮箱已注册");
        }

        // 检查昵称是否有空格（不能全是空格）
        if (request.getNickname().trim().isEmpty()) {
            throw new BusinessException(400, "昵称不能全为空格");
        }

        // 创建用户
        User user = new User();
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        userMapper.insert(user);

        // 签发 Token（Sa-Token JWT 模式）
        StpUtil.login(user.getId(), new SaLoginModel().setExtra("nickname", user.getNickname()));
        String token = StpUtil.getTokenValue();

        log.info("用户注册成功 id={}, email={}", user.getId(), user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .user(toDTO(user))
                .build();
    }

    /**
     * 登录
     */
    public LoginResponse login(UserLoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail().trim()));
        if (user == null) {
            throw new BusinessException(400, "邮箱或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "邮箱或密码错误");
        }

        // 签发 Token
        StpUtil.login(user.getId(), new SaLoginModel().setExtra("nickname", user.getNickname()));
        String token = StpUtil.getTokenValue();

        log.info("用户登录成功 id={}, email={}", user.getId(), user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .user(toDTO(user))
                .build();
    }

    /**
     * 获取当前登录用户信息
     */
    public UserDTO getCurrentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return toDTO(user);
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
