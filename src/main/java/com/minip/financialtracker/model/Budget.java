package com.minip.financialtracker.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Budget {
    private final Category category;
    private final BigDecimal limit;

    public Budget(Category category, BigDecimal limit) {
        this.category = Objects.requireNonNull(category, "category");
        Objects.requireNonNull(limit, "limit");
        if (limit.signum() <= 0) {
            throw new IllegalArgumentException("limit must be greater than zero");
        }
        this.limit = limit;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getLimit() {
        return limit;
    }
}
