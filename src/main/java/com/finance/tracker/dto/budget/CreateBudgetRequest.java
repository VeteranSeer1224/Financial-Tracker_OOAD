package com.finance.tracker.dto.budget;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateBudgetRequest {
    @NotBlank
    private String name;

    @Positive
    private double spendingLimit;

    @NotBlank
    private String period;

    @PositiveOrZero
    private double currentSpending;
}
