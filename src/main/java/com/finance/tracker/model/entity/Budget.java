package com.finance.tracker.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID budgetId;

    private String name;
    private double spendingLimit;
    private String period;
    private double currentSpending;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    public double getRemainingBalance() {
        return spendingLimit - currentSpending;
    }

    public double getRemainingBudget() {
        return getRemainingBalance();
    }

    public boolean isExceeded() {
        return spendingLimit > 0 && currentSpending >= spendingLimit;
    }

    public void addSpending(double amount) {
        if (amount > 0) {
            this.currentSpending += amount;
        }
    }
}
