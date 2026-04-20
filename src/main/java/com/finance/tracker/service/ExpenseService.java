package com.finance.tracker.service;

import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.exception.ValidationException;
import com.finance.tracker.model.entity.Budget;
import com.finance.tracker.model.entity.Category;
import com.finance.tracker.model.entity.Expense;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.model.enums.CategoryType;
import com.finance.tracker.model.payment.PaymentMethod;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.PaymentMethodRepository;
import com.finance.tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public Expense logExpense(
            UUID userId,
            Expense expense,
            UUID paymentMethodId,
            CategoryType categoryType,
            String categoryName,
            Double budgetLimit) {
        if (expense.getAmount() <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        if (expense.getDate() == null || expense.getDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Date cannot be in the future");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found: " + paymentMethodId));

        Category category = categoryRepository
                .findByUserUserIdAndType(userId, categoryType)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(categoryName == null || categoryName.isBlank() ? categoryType.name() : categoryName)
                        .type(categoryType)
                        .budgetLimit(budgetLimit == null ? 0 : budgetLimit)
                        .currentSpending(0)
                        .user(user)
                        .build()));

        expense.setUser(user);
        expense.setCategory(category);
        expense.setPaymentMethod(paymentMethod);
        Expense savedExpense = expenseRepository.save(expense);

        category.trackSpending(expense.getAmount());
        categoryRepository.save(category);
        triggerBudgetNotification(userId, category);

        return savedExpense;
    }

    public List<Expense> getUserExpenses(UUID userId) {
        return expenseRepository.findByUserUserId(userId);
    }

    private void triggerBudgetNotification(UUID userId, Category category) {
        if (category.getBudgetLimit() <= 0) {
            return;
        }
        double ratio = category.getCurrentSpending() / category.getBudgetLimit();
        Budget syntheticBudget = Budget.builder()
                .budgetId(category.getCategoryId())
                .name(category.getName())
                .spendingLimit(category.getBudgetLimit())
                .currentSpending(category.getCurrentSpending())
                .build();
        if (ratio >= 1.0) {
            notificationService.notifyBudgetExceeded(syntheticBudget, userId);
        } else if (ratio >= 0.8) {
            notificationService.notifyBudgetWarning(syntheticBudget, userId);
        }
    }
}
