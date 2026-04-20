package com.finance.tracker.model.entity;

import com.finance.tracker.model.enums.BillingFrequency;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class BillingCycle {
    @Enumerated(EnumType.STRING)
    private BillingFrequency frequency;

    private Integer customIntervalDays;

    private LocalDate startDate;

    public LocalDate getNextPaymentDate() {
        if (startDate == null || frequency == null) {
            return null;
        }
        LocalDate now = LocalDate.now();
        LocalDate candidate = startDate;
        if (!candidate.isBefore(now)) {
            return candidate;
        }
        while (candidate.isBefore(now)) {
            candidate = addCycle(candidate);
        }
        return candidate;
    }

    public long getDaysUntilNextPayment() {
        LocalDate next = getNextPaymentDate();
        return next == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), next);
    }

    public double getProjectedAnnualCost(double cost) {
        if (cost <= 0 || frequency == null) {
            return 0;
        }
        return switch (frequency) {
            case WEEKLY -> cost * 52;
            case MONTHLY -> cost * 12;
            case QUARTERLY -> cost * 4;
            case ANNUALLY -> cost;
            case CUSTOM -> {
                if (customIntervalDays == null || customIntervalDays <= 0) {
                    yield 0;
                }
                yield cost * (365.0 / customIntervalDays);
            }
        };
    }

    private LocalDate addCycle(LocalDate date) {
        return switch (frequency) {
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case ANNUALLY -> date.plusYears(1);
            case CUSTOM -> date.plusDays(customIntervalDays == null ? 0 : customIntervalDays);
        };
    }
}
