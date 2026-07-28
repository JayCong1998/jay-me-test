package com.jaymetest.controller.admin;

import com.jaymetest.model.admin.PageResponse;
import com.jaymetest.model.dto.R;
import com.jaymetest.model.entity.User;
import com.jaymetest.service.admin.AdminUserQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Users")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserQueryService adminUserQueryService;

    @GetMapping
    public R<PageResponse<User>> list(@RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return R.ok(adminUserQueryService.list(keyword, page, size));
    }
}
