package com.minip.financialtracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Transaction {
    private final UUID id;
    private final TransactionType type;
    private final String description;
    private final BigDecimal amount;
    private final Category category;
    private final LocalDate date;

    public Transaction(TransactionType type, String description, BigDecimal amount, Category category, LocalDate date) {
        this.id = UUID.randomUUID();
        this.type = Objects.requireNonNull(type, "type");
        this.description = requireText(description, "description");
        this.amount = requireAmount(amount);
        this.category = Objects.requireNonNull(category, "category");
        this.date = Objects.requireNonNull(date, "date");
    }

    public UUID getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal signedAmount() {
        return type == TransactionType.INCOME ? amount : amount.negate();
    }

    public String toDisplayString() {
        return String.format("%s | %s | %s | %s | %s",
                date,
                type,
                category,
                amount.toPlainString(),
                description);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
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
}
