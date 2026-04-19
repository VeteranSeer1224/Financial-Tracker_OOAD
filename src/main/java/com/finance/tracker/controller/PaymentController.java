package com.finance.tracker.controller;

import com.finance.tracker.model.payment.PaymentMethod;
import com.finance.tracker.service.PaymentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/payments")
public class PaymentController {

    private final PaymentManager paymentManager;

    @Autowired
    public PaymentController(PaymentManager paymentManager) {
        this.paymentManager = paymentManager;
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethod>> getUserPaymentMethods(@PathVariable String userId) {
        List<PaymentMethod> methods = paymentManager.getUserPaymentMethods(userId);
        return ResponseEntity.ok(methods);
    }

    @PostMapping
    public ResponseEntity<?> addPaymentMethod(
            @PathVariable String userId, 
            @RequestBody PaymentMethod paymentMethod) {
        try {
            PaymentMethod savedMethod = paymentManager.addPaymentMethod(userId, paymentMethod);
            return new ResponseEntity<>(savedMethod, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{pmId}")
    public ResponseEntity<?> removePaymentMethod(
            @PathVariable String userId, 
            @PathVariable String pmId) {
        try {
            paymentManager.removePaymentMethod(userId, pmId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
