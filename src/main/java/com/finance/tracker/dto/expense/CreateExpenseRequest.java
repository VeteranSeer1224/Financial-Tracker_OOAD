package com.finance.tracker.dto.expense;

import com.finance.tracker.model.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateExpenseRequest {
    @Positive
    private double amount;

    @NotNull
    private LocalDate date;

    @NotBlank
    private String description;

    @NotNull
    private UUID paymentMethodId;

    @NotNull
    private CategoryType categoryType;

    private String categoryName;
    private Double budgetLimit;
}
