package com.jaymetest.controller.admin;

import com.jaymetest.model.admin.AdminDashboardOverview;
import com.jaymetest.model.dto.R;
import com.jaymetest.service.admin.AdminDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Dashboard")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public R<AdminDashboardOverview> overview() {
        return R.ok(adminDashboardService.getOverview());
    }
}
