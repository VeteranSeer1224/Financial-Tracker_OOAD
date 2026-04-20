package com.finance.tracker.repository;

import com.finance.tracker.model.entity.Budget;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByUserUserId(UUID userId);
}
