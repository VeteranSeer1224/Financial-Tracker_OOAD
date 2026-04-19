package com.finance.tracker.model.entity;

import com.finance.tracker.model.enums.BillingFrequency;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillingCycleTest {

    @Test
    void calculateNextPaymentDate_forMonthlyInterval_handlesEndOfMonth() {
        BillingCycle cycle = new BillingCycle(
                BillingFrequency.MONTHLY,
                null,
                LocalDate.of(2024, 1, 31)
        );

        LocalDate next = cycle.calculateNextPaymentDate(LocalDate.of(2024, 1, 31));

        assertEquals(LocalDate.of(2024, 2, 29), next);
    }

    @Test
    void calculateNextPaymentDate_forCustom45Days_returnsExpectedDate() {
        BillingCycle cycle = new BillingCycle(
                BillingFrequency.CUSTOM_DAYS,
                45,
                LocalDate.of(2026, 1, 1)
        );

        LocalDate next = cycle.calculateNextPaymentDate(LocalDate.of(2026, 1, 1));

        assertEquals(LocalDate.of(2026, 2, 15), next);
    }

    @Test
    void calculateNextPaymentDate_rollsForwardUntilAfterReferenceDate() {
        BillingCycle cycle = new BillingCycle(
                BillingFrequency.CUSTOM_DAYS,
                45,
                LocalDate.of(2026, 1, 1)
        );

        LocalDate next = cycle.calculateNextPaymentDate(LocalDate.of(2026, 3, 1));

        assertEquals(LocalDate.of(2026, 4, 1), next);
    }

    @Test
    void constructor_withCustomFrequencyAndInvalidInterval_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new BillingCycle(BillingFrequency.CUSTOM_DAYS, 0, LocalDate.of(2026, 1, 1))
        );
    }

    @Test
    void rollForwardFrom_updatesNextPaymentDateField() {
        BillingCycle cycle = new BillingCycle(
                BillingFrequency.MONTHLY,
                null,
                LocalDate.of(2026, 1, 10)
        );

        LocalDate rolled = cycle.rollForwardFrom(LocalDate.of(2026, 2, 10));

        assertEquals(LocalDate.of(2026, 3, 10), rolled);
        assertEquals(LocalDate.of(2026, 3, 10), cycle.getNextPaymentDate());
    }
}
