package com.finance.tracker.repository;

import com.finance.tracker.model.entity.Expense;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByUserUserId(UUID userId);

    List<Expense> findByUserUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    List<Expense> findTop10ByUserUserIdOrderByAmountDesc(UUID userId);

    Optional<Expense> findByExpenseIdAndUserUserId(UUID expenseId, UUID userId);
}
