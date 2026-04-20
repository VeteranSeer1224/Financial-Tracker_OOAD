package com.finance.tracker.dto.subscription;

import com.finance.tracker.model.enums.BillingFrequency;
import com.finance.tracker.model.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateSubscriptionRequest {
    @NotBlank
    private String serviceName;

    @Positive
    private double cost;

    @NotNull
    private SubscriptionStatus status;

    @NotNull
    private BillingFrequency frequency;

    private Integer customIntervalDays;

    @NotNull
    private LocalDate startDate;
}
