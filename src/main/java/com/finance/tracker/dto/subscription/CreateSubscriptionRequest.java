package com.finance.tracker.dto.subscription;

import com.finance.tracker.model.enums.BillingFrequency;
import com.finance.tracker.model.enums.CategoryType;
import com.finance.tracker.model.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    @NotBlank
    private String serviceName;

    @Positive
    private double cost;

    @NotNull
    private UUID paymentMethodId;

    @NotNull
    private CategoryType categoryType;

    private String categoryName;

    private SubscriptionStatus status;
    private LocalDate lastAccessDate;

    @NotNull
    private BillingFrequency frequency;

    private Integer customIntervalDays;

    @NotNull
    private LocalDate startDate;
}
