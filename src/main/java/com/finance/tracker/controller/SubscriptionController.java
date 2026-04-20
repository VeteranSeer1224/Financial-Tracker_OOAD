package com.finance.tracker.controller;

import com.finance.tracker.dto.subscription.CreateSubscriptionRequest;
import com.finance.tracker.dto.subscription.UpdateSubscriptionRequest;
import com.finance.tracker.model.entity.BillingCycle;
import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.enums.SubscriptionStatus;
import com.finance.tracker.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/users/{userId}/subscriptions")
    public Map<String, Object> createSubscription(@PathVariable UUID userId, @Valid @RequestBody CreateSubscriptionRequest request) {
        BillingCycle billingCycle = BillingCycle.builder()
                .frequency(request.getFrequency())
                .customIntervalDays(request.getCustomIntervalDays())
                .startDate(request.getStartDate())
                .build();
        Subscription subscription = Subscription.builder()
                .serviceName(request.getServiceName())
                .cost(request.getCost())
                .status(request.getStatus())
                .lastAccessDate(request.getLastAccessDate())
                .billingCycle(billingCycle)
                .build();

        Subscription saved = subscriptionService.createSubscription(
                userId, subscription, request.getPaymentMethodId(), request.getCategoryType(), request.getCategoryName());
        return Map.of(
                "subscription", saved,
                "nextPaymentDate", billingCycle.getNextPaymentDate(),
                "projectedAnnualCost", billingCycle.getProjectedAnnualCost(saved.getCost()));
    }

    @GetMapping("/users/{userId}/subscriptions")
    public List<Subscription> getUserSubscriptions(@PathVariable UUID userId) {
        return subscriptionService.getUserSubscriptions(userId);
    }

    @GetMapping("/subscriptions/{subscriptionId}")
    public Subscription getSubscription(@PathVariable UUID subscriptionId) {
        return subscriptionService.getSubscription(subscriptionId);
    }

    @PatchMapping("/subscriptions/{subscriptionId}/status")
    public Subscription updateStatus(@PathVariable UUID subscriptionId, @RequestParam SubscriptionStatus status) {
        return subscriptionService.updateSubscriptionStatus(subscriptionId, status);
    }

    @PutMapping("/users/{userId}/subscriptions/{subscriptionId}")
    public Subscription updateSubscription(
            @PathVariable UUID userId,
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        BillingCycle billingCycle = BillingCycle.builder()
                .frequency(request.getFrequency())
                .customIntervalDays(request.getCustomIntervalDays())
                .startDate(request.getStartDate())
                .build();
        return subscriptionService.updateSubscription(
                userId,
                subscriptionId,
                request.getServiceName(),
                request.getCost(),
                request.getStatus(),
                billingCycle);
    }
}
