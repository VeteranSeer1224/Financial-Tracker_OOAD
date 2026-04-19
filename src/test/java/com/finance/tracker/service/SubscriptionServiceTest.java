package com.finance.tracker.service;

import com.finance.tracker.model.entity.BillingCycle;
import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.enums.BillingFrequency;
import com.finance.tracker.model.enums.SubscriptionStatus;
import com.finance.tracker.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-19T00:00:00Z"), ZoneId.of("UTC"));
        subscriptionService = new SubscriptionService(subscriptionRepository, fixedClock);
    }

    @Test
    void getSubscriptionsRenewingIn_returnsOnlyActiveSubscriptionsWithinWindow() {
        Subscription renewsIn5Days = buildSubscription(
                1L,
                "Video Stream",
                SubscriptionStatus.ACTIVE,
                BillingFrequency.CUSTOM_DAYS,
                45,
                LocalDate.of(2026, 3, 5)
        );
        Subscription renewsAfterWindow = buildSubscription(
                2L,
                "Cloud Storage",
                SubscriptionStatus.ACTIVE,
                BillingFrequency.MONTHLY,
                null,
                LocalDate.of(2026, 4, 25)
        );

        when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(renewsIn5Days, renewsAfterWindow));

        List<Subscription> result = subscriptionService.getSubscriptionsRenewingIn(7);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getSubscriptionsRenewingIn_withNegativeDays_throws() {
        assertThrows(IllegalArgumentException.class, () -> subscriptionService.getSubscriptionsRenewingIn(-1));
    }

    @Test
    void pauseSubscription_updatesStatusAndSaves() {
        Subscription subscription = buildSubscription(
                3L,
                "Music",
                SubscriptionStatus.ACTIVE,
                BillingFrequency.MONTHLY,
                null,
                LocalDate.of(2026, 5, 1)
        );

        when(subscriptionRepository.findById(3L)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subscription updated = subscriptionService.pauseSubscription(3L);

        assertEquals(SubscriptionStatus.PAUSED, updated.getStatus());
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    void reactivateSubscription_fromCancelled_throws() {
        Subscription subscription = buildSubscription(
                4L,
                "Cancelled Plan",
                SubscriptionStatus.CANCELLED,
                BillingFrequency.MONTHLY,
                null,
                LocalDate.of(2026, 5, 1)
        );

        when(subscriptionRepository.findById(4L)).thenReturn(Optional.of(subscription));

        assertThrows(IllegalStateException.class, () -> subscriptionService.reactivateSubscription(4L));
    }

    private Subscription buildSubscription(
            Long id,
            String name,
            SubscriptionStatus status,
            BillingFrequency frequency,
            Integer intervalDays,
            LocalDate nextPaymentDate
    ) {
        BillingCycle cycle = new BillingCycle(frequency, intervalDays, nextPaymentDate);
        return new Subscription(id, name, BigDecimal.TEN, status, cycle);
    }
}
