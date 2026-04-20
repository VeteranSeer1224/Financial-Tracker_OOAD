package com.finance.tracker.controller;

import com.finance.tracker.dto.category.UpdateCategoryBudgetLimitRequest;
import com.finance.tracker.model.entity.Category;
import com.finance.tracker.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/users/{userId}/categories")
    public List<Category> getCategories(@PathVariable UUID userId) {
        return categoryService.getUserCategories(userId);
    }

    @PatchMapping("/users/{userId}/categories/{categoryId}")
    public Category updateBudgetLimit(
            @PathVariable UUID userId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryBudgetLimitRequest request) {
        return categoryService.updateBudgetLimit(userId, categoryId, request.getBudgetLimit());
    }
}
