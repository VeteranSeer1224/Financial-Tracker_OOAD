package com.finance.tracker.controller;

import com.finance.tracker.dto.budget.CreateBudgetRequest;
import com.finance.tracker.dto.budget.UpdateBudgetRequest;
import com.finance.tracker.model.entity.Budget;
import com.finance.tracker.service.BudgetService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping("/users/{userId}/budgets")
    public Budget createBudget(@PathVariable UUID userId, @Valid @RequestBody CreateBudgetRequest request) {
        Budget budget = Budget.builder()
                .name(request.getName())
                .spendingLimit(request.getSpendingLimit())
                .period(request.getPeriod())
                .currentSpending(request.getCurrentSpending())
                .build();
        return budgetService.createBudget(userId, budget);
    }

    @GetMapping("/users/{userId}/budgets")
    public List<Budget> getBudgets(@PathVariable UUID userId) {
        return budgetService.getUserBudgets(userId);
    }

    @PutMapping("/users/{userId}/budgets/{budgetId}")
    public Budget updateBudget(
            @PathVariable UUID userId, @PathVariable UUID budgetId, @Valid @RequestBody UpdateBudgetRequest request) {
        Budget budget = Budget.builder()
                .name(request.getName())
                .spendingLimit(request.getSpendingLimit())
                .period(request.getPeriod())
                .currentSpending(request.getCurrentSpending())
                .build();
        return budgetService.updateBudget(userId, budgetId, budget);
    }
}
