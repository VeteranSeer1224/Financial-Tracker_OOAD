package com.finance.tracker.controller;

import com.finance.tracker.model.payment.PaymentMethod;
import com.finance.tracker.service.PaymentManager;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentManager paymentManager;

    @PostMapping("/users/{userId}/payment-methods")
    public PaymentMethod addPaymentMethod(@PathVariable UUID userId, @RequestBody PaymentMethod paymentMethod) {
        return paymentManager.addPaymentMethod(userId, paymentMethod);
    }

    @GetMapping("/users/{userId}/payment-methods")
    public List<PaymentMethod> getPaymentMethods(@PathVariable UUID userId) {
        return paymentManager.getUserPaymentMethods(userId);
    }

    @DeleteMapping("/users/{userId}/payment-methods/{paymentMethodId}")
    public void removePaymentMethod(@PathVariable UUID userId, @PathVariable UUID paymentMethodId) {
        paymentManager.removePaymentMethod(userId, paymentMethodId);
    }

    @PostMapping("/payment-methods/{paymentMethodId}/process")
    public Map<String, Boolean> processPayment(@PathVariable UUID paymentMethodId, @RequestParam @Positive double amount) {
        return Map.of("processed", paymentManager.processPayment(amount, paymentMethodId));
    }
}
