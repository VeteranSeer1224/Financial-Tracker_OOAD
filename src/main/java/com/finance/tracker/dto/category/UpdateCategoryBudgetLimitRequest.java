package com.finance.tracker.dto.category;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class UpdateCategoryBudgetLimitRequest {
    @PositiveOrZero
    private double budgetLimit;
}
