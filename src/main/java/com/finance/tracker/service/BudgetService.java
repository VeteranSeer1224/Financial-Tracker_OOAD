package com.finance.tracker.service;

import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.model.entity.Budget;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    @Transactional
    public Budget createBudget(UUID userId, Budget budget) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        budget.setUser(user);
        return budgetRepository.save(budget);
    }

    public List<Budget> getUserBudgets(UUID userId) {
        return budgetRepository.findByUserUserId(userId);
    }

    @Transactional
    public Budget updateBudget(UUID userId, UUID budgetId, Budget input) {
        Budget budget = budgetRepository.findByBudgetIdAndUserUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + budgetId));
        budget.setName(input.getName());
        budget.setSpendingLimit(input.getSpendingLimit());
        budget.setPeriod(input.getPeriod());
        budget.setCurrentSpending(input.getCurrentSpending());
        return budgetRepository.save(budget);
    }
}
