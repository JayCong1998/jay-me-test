package com.jaymetest.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.AdminUserMapper;
import com.jaymetest.model.admin.AdminDTO;
import com.jaymetest.model.admin.AdminLoginRequest;
import com.jaymetest.model.admin.AdminLoginResponse;
import com.jaymetest.model.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final AdminTokenService adminTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminLoginResponse login(AdminLoginRequest request) {
        String username = request.getUsername().trim();
        AdminUser admin = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username));
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(admin.getEnabled())) {
            throw new BusinessException(403, "管理员账号已禁用");
        }

        String token = adminTokenService.login(admin.getId());
        admin.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateById(admin);

        log.info("admin login success id={}, username={}", admin.getId(), admin.getUsername());
        return AdminLoginResponse.builder()
                .token(token)
                .admin(toDTO(admin))
                .build();
    }

    public AdminDTO getCurrentAdmin() {
        long adminId = adminTokenService.getLoginIdAsLong();
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null || !Integer.valueOf(1).equals(admin.getEnabled())) {
            throw new BusinessException(401, "管理员不存在或已禁用");
        }
        return toDTO(admin);
    }

    public void logout() {
        adminTokenService.logout();
    }

    private AdminDTO toDTO(AdminUser admin) {
        return AdminDTO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .role(admin.getRole())
                .build();
    }
}
