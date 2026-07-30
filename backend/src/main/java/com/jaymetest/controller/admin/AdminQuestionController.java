package com.jaymetest.controller.admin;

import com.jaymetest.model.admin.AdminQuestionRequest;
import com.jaymetest.model.admin.PageResponse;
import com.jaymetest.model.admin.QuestionOptionRebalanceResponse;
import com.jaymetest.model.dto.R;
import com.jaymetest.model.entity.Question;
import com.jaymetest.service.admin.AdminQuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Questions")
@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    @GetMapping
    public R<PageResponse<Question>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String category,
                                          @RequestParam(required = false) String difficulty,
                                          @RequestParam(required = false) String album,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return R.ok(adminQuestionService.list(keyword, category, difficulty, album, page, size));
    }

    @PostMapping
    public R<Question> create(@Valid @RequestBody AdminQuestionRequest request) {
        return R.ok(adminQuestionService.create(request));
    }

    @GetMapping("/{id}")
    public R<Question> get(@PathVariable long id) {
        return R.ok(adminQuestionService.get(id));
    }

    @PutMapping("/{id}")
    public R<Question> update(@PathVariable long id, @Valid @RequestBody AdminQuestionRequest request) {
        return R.ok(adminQuestionService.update(id, request));
    }

    @PostMapping("/rebalance-options")
    public R<QuestionOptionRebalanceResponse> rebalanceOptions() {
        return R.ok(adminQuestionService.rebalanceOptions());
    }
}
