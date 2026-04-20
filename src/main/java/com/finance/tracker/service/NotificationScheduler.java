package com.finance.tracker.service;

import com.finance.tracker.model.entity.Budget;
import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.enums.NotificationType;
import com.finance.tracker.model.enums.SubscriptionStatus;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final SubscriptionRepository subscriptionRepository;
    private final BudgetRepository budgetRepository;
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

        List<Budget> budgets = budgetRepository.findAllWithUser();
        for (Budget budget : budgets) {
            if (budget.getSpendingLimit() <= 0 || budget.getUser() == null) {
                continue;
            }

            String referenceId = budget.getBudgetId().toString();
            UUID userId = budget.getUser().getUserId();
            if (budget.isExceeded()
                    && !notificationService.isDuplicate(userId, NotificationType.BUDGET_EXCEEDED, referenceId)) {
                notificationService.notifyBudgetExceeded(budget, userId);
            } else if (budget.checkThreshold(0.8)
                    && budget.getRemainingBudget() >= 0
                    && !notificationService.isDuplicate(userId, NotificationType.BUDGET_WARNING, referenceId)) {
                notificationService.notifyBudgetWarning(budget, userId);
            }
        }
    }
}
