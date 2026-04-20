package com.finance.tracker.repository;

import com.finance.tracker.model.entity.Budget;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByUserUserId(UUID userId);

    Optional<Budget> findByBudgetIdAndUserUserId(UUID budgetId, UUID userId);

    List<Budget> findByUserUserIdAndNameIgnoreCase(UUID userId, String name);

    @Query("select b from Budget b join fetch b.user")
    List<Budget> findAllWithUser();
}
