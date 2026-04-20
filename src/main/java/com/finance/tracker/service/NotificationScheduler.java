package com.finance.tracker.service;

import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.enums.SubscriptionStatus;
import com.finance.tracker.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 86400000)
    public void runScheduledCheck() {
        List<Subscription> dueSubscriptions =
                subscriptionRepository.findByStatusAndNextPaymentDateLessThanEqual(SubscriptionStatus.ACTIVE, LocalDate.now().plusDays(3));
        for (Subscription subscription : dueSubscriptions) {
            if (subscription.getBillingCycle() == null || subscription.getBillingCycle().getDaysUntilNextPayment() > 3) {
                continue;
            }
            notificationService.notifySubscriptionRenewal(subscription, subscription.getUser().getUserId());
        }
    }
}
