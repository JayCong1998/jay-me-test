package com.jaymetest.service;

import com.jaymetest.exception.BusinessException;
import com.jaymetest.mapper.UserMapper;
import com.jaymetest.model.dto.UserRegisterRequest;
import com.jaymetest.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerRejectsNicknameAlreadyUsed() {
        User existingUser = new User();
        existingUser.setNickname("Jay");
        when(userMapper.findByNickname("Jay")).thenReturn(existingUser);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("123456");
        request.setNickname("Jay");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.register(request));

        assertEquals(400, ex.getCode());
        assertEquals("该昵称已被使用", ex.getMessage());
    }
}
