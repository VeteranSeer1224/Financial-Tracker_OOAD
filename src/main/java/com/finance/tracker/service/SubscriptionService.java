package com.finance.tracker.service;

import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.exception.ValidationException;
import com.finance.tracker.model.entity.Category;
import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.model.enums.CategoryType;
import com.finance.tracker.model.enums.SubscriptionStatus;
import com.finance.tracker.model.payment.PaymentMethod;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.PaymentMethodRepository;
import com.finance.tracker.repository.SubscriptionRepository;
import com.finance.tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Subscription createSubscription(
            UUID userId, Subscription subscription, UUID paymentMethodId, CategoryType categoryType, String categoryName) {
        if (subscription.getCost() <= 0) {
            throw new ValidationException("Subscription cost must be positive");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found: " + paymentMethodId));
        Category category = categoryRepository.findByUserUserIdAndType(userId, categoryType).orElseGet(() -> categoryRepository.save(
                Category.builder()
                        .name(categoryName == null || categoryName.isBlank() ? categoryType.name() : categoryName)
                        .type(categoryType)
                        .budgetLimit(0)
                        .currentSpending(0)
                        .user(user)
                        .build()));

        subscription.setUser(user);
        subscription.setPaymentMethod(paymentMethod);
        subscription.setCategory(category);
        if (subscription.getStatus() == null) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        return subscriptionRepository.save(subscription);
    }

    public Subscription getSubscription(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + subscriptionId));
    }

    public List<Subscription> getUserSubscriptions(UUID userId) {
        return subscriptionRepository.findByUserUserId(userId);
    }

    @Transactional
    public Subscription updateSubscriptionStatus(UUID subscriptionId, SubscriptionStatus status) {
        Subscription subscription = getSubscription(subscriptionId);
        subscription.setStatus(status);
        return subscriptionRepository.save(subscription);
    }
}
