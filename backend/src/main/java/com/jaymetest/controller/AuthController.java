package com.jaymetest.controller;

import com.jaymetest.model.dto.*;
import com.jaymetest.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证 API
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "注册")
    @PostMapping("/register")
    public R<LoginResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        LoginResponse response = authService.register(request);
        return R.ok(response);
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        LoginResponse response = authService.login(request);
        return R.ok(response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public R<UserDTO> me() {
        UserDTO user = authService.getCurrentUser();
        return R.ok(user);
    }
}
