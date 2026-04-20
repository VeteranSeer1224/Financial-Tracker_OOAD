package com.minip.financialtracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import java.util.UUID;

public final class Subscription {
    private final UUID id;
    private final String name;
    private final BigDecimal amount;
    private final Category category;
    private final int billingDay;
    private final LocalDate createdOn;
    private boolean active;

    public Subscription(String name, BigDecimal amount, Category category, int billingDay) {
        this.id = UUID.randomUUID();
        this.name = requireText(name);
        this.amount = requireAmount(amount);
        this.category = Objects.requireNonNull(category, "category");
        this.billingDay = requireBillingDay(billingDay);
        this.createdOn = LocalDate.now();
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public int getBillingDay() {
        return billingDay;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate dueDateFor(YearMonth month) {
        int day = Math.min(billingDay, month.lengthOfMonth());
        return month.atDay(day);
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }
        return value.trim();
    }

    private static BigDecimal requireAmount(BigDecimal value) {
        Objects.requireNonNull(value, "amount");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        return value;
    }

    private static int requireBillingDay(int value) {
        if (value < 1 || value > 31) {
            throw new IllegalArgumentException("billing day must be between 1 and 31");
        }
        return value;
    }
}
