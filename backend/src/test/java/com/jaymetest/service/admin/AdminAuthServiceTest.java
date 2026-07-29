package com.jaymetest.service.admin;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.AdminUserMapper;
import com.jaymetest.model.admin.AdminLoginRequest;
import com.jaymetest.model.admin.AdminLoginResponse;
import com.jaymetest.model.entity.AdminUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminUserMapper adminUserMapper;

    @Mock
    private AdminTokenService adminTokenService;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @Test
    void loginReturnsAdminProfileAndTokenWhenPasswordMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        AdminUser admin = new AdminUser();
        admin.setId(7L);
        admin.setUsername("admin");
        admin.setPassword(encoder.encode("secret123"));
        admin.setNickname("Operator");
        admin.setRole("SUPER_ADMIN");
        admin.setEnabled(1);

        when(adminUserMapper.selectOne(any())).thenReturn(admin);
        when(adminTokenService.login(7L, "Operator")).thenReturn("admin-token");

        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername(" admin ");
        request.setPassword("secret123");

        AdminLoginResponse response = adminAuthService.login(request);

        assertEquals("admin-token", response.getToken());
        assertEquals(7L, response.getAdmin().getId());
        assertEquals("admin", response.getAdmin().getUsername());
        assertEquals("SUPER_ADMIN", response.getAdmin().getRole());
        verify(adminTokenService).login(7L, "Operator");
        verify(adminUserMapper).updateById(admin);
    }

    @Test
    void loginRejectsDisabledAdmin() {
        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("secret123"));
        admin.setEnabled(0);

        when(adminUserMapper.selectOne(any())).thenReturn(admin);

        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("admin");
        request.setPassword("secret123");

        BusinessException ex = assertThrows(BusinessException.class, () -> adminAuthService.login(request));

        assertEquals(403, ex.getCode());
    }
}
