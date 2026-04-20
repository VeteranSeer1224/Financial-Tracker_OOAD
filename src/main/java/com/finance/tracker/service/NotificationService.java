package com.finance.tracker.service;

import com.finance.tracker.infrastructure.notification.INotificationSender;
import com.finance.tracker.model.entity.Budget;
import com.finance.tracker.model.entity.Notification;
import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.model.enums.NotificationType;
import com.finance.tracker.repository.NotificationRepository;
import com.finance.tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final INotificationSender sender;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notifyBudgetExceeded(Budget budget, UUID userId) {
        createAndSend(
                userId,
                NotificationType.BUDGET_EXCEEDED,
                "Budget '" + budget.getName() + "' exceeded by " + Math.abs(budget.getRemainingBalance()),
                budget.getBudgetId().toString());
    }

    @Transactional
    public void notifyBudgetWarning(Budget budget, UUID userId) {
        createAndSend(
                userId,
                NotificationType.BUDGET_WARNING,
                "Budget '" + budget.getName() + "' has reached 80% of the limit.",
                budget.getBudgetId().toString());
    }

    @Transactional
    public void notifySubscriptionRenewal(Subscription sub, UUID userId) {
        createAndSend(
                userId,
                NotificationType.RENEWAL_REMINDER,
                "Subscription '" + sub.getServiceName() + "' renews in " + sub.getBillingCycle().getDaysUntilNextPayment()
                        + " day(s).",
                sub.getSubscriptionId().toString());
    }

    public boolean isDuplicate(UUID userId, NotificationType type, String refId) {
        return notificationRepository.existsByUserUserIdAndTypeAndReferenceIdAndReadFalse(userId, type, refId);
    }

    @Transactional
    public Notification createAndSend(UUID userId, NotificationType type, String message, String referenceId) {
        User user = userRepository.findById(userId).orElseThrow();
        boolean enabled = user.getNotificationPreferences().getOrDefault(type.name(), true);
        boolean duplicate = isDuplicate(userId, type, referenceId);
        if (duplicate) {
            return null;
        }
        Notification notification = Notification.builder()
                .type(type)
                .message(message)
                .read(false)
                .referenceId(referenceId)
                .silentlyDismissed(!enabled)
                .user(user)
                .build();
        Notification saved = notificationRepository.save(notification);
        if (enabled) {
            sender.send(message, userId.toString());
        }
        return saved;
    }
}
