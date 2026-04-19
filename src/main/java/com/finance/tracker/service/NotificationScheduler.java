package com.finance.tracker.service;

import com.finance.tracker.infrastructure.notification.INotificationSender;
import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.repository.SubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationScheduler {

    private final INotificationSender notificationSender;
    private final SubscriptionRepository subscriptionRepository;

    // Inject the SubscriptionRepository your teammate created
    public NotificationScheduler(INotificationSender notificationSender, SubscriptionRepository subscriptionRepository) {
        this.notificationSender = notificationSender;
        this.subscriptionRepository = subscriptionRepository;
    }

    // Run every day at 8:00 AM (Keep it at 60000ms for testing if you prefer)
    @Scheduled(fixedRate = 60000) 
    public void evaluateAlerts() {
        System.out.println("Running scheduled notification check...");

        // Fetch all subscriptions (Or ask your teammate if they made a specific method for active ones)
        List<Subscription> subscriptions = (List<Subscription>) subscriptionRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Subscription sub : subscriptions) {
            // Assuming your teammate created a getNextPaymentDate() method in Subscription or BillingCycle
            LocalDate nextPayment = sub.getBillingCycle().getNextPaymentDate();
            
            if (nextPayment != null) {
                long daysUntilRenewal = ChronoUnit.DAYS.between(today, nextPayment);

                // If renewing in exactly 3 days (or fewer), send an alert
                if (daysUntilRenewal >= 0 && daysUntilRenewal <= 3) {
                    String message = String.format("Your %s subscription renews in %d days for $%.2f.", 
                            sub.getName(), daysUntilRenewal, sub.getCost());
                            
                    notificationSender.send("user@example.com", "Upcoming Charge Warning", message);
                }
            }
        }
    }
}