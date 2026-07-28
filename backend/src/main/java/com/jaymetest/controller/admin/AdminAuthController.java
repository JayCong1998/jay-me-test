package com.jaymetest.controller.admin;

import com.jaymetest.model.admin.AdminDTO;
import com.jaymetest.model.admin.AdminLoginRequest;
import com.jaymetest.model.admin.AdminLoginResponse;
import com.jaymetest.model.dto.R;
import com.jaymetest.service.admin.AdminAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Auth")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public R<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return R.ok(adminAuthService.login(request));
    }

    @GetMapping("/me")
    public R<AdminDTO> me() {
        return R.ok(adminAuthService.getCurrentAdmin());
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        adminAuthService.logout();
        return R.ok();
    }
}
