package com.jaymetest.controller.admin;

import com.jaymetest.model.admin.AdminRecordDTO;
import com.jaymetest.model.admin.PageResponse;
import com.jaymetest.model.dto.R;
import com.jaymetest.service.admin.AdminRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Admin Records")
@RestController
@RequestMapping("/api/admin/records")
@RequiredArgsConstructor
public class AdminRecordController {

    private final AdminRecordService adminRecordService;

    @GetMapping
    public R<PageResponse<AdminRecordDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endAt,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(adminRecordService.list(keyword, mode, startAt, endAt, page, size));
    }
}
