package com.finance.tracker.controller;

import com.finance.tracker.dto.expense.CreateExpenseRequest;
import com.finance.tracker.model.entity.Expense;
import com.finance.tracker.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping("/users/{userId}/expenses")
    public Expense createExpense(@PathVariable UUID userId, @Valid @RequestBody CreateExpenseRequest request) {
        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .description(request.getDescription())
                .build();
        return expenseService.logExpense(
                userId,
                expense,
                request.getPaymentMethodId(),
                request.getCategoryType(),
                request.getCategoryName(),
                request.getBudgetLimit());
    }

    @GetMapping("/users/{userId}/expenses")
    public List<Expense> getExpenses(@PathVariable UUID userId) {
        return expenseService.getUserExpenses(userId);
    }

    @GetMapping("/users/{userId}/expenses/{expenseId}")
    public Expense getExpense(@PathVariable UUID userId, @PathVariable UUID expenseId) {
        return expenseService.getExpenseById(expenseId);
    }

    @DeleteMapping("/users/{userId}/expenses/{expenseId}")
    public void deleteExpense(@PathVariable UUID userId, @PathVariable UUID expenseId) {
        expenseService.deleteExpense(userId, expenseId);
    }
}
