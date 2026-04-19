package com.finance.tracker.controller;

import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
	private final SubscriptionService subscriptionService;

	public SubscriptionController(SubscriptionService subscriptionService) {
		this.subscriptionService = subscriptionService;
	}

	@GetMapping("/renewing")
	public ResponseEntity<List<Subscription>> getRenewingSubscriptions(@RequestParam int days) {
		return ResponseEntity.ok(subscriptionService.getSubscriptionsRenewingIn(days));
	}

	@PatchMapping("/{id}/pause")
	public ResponseEntity<Subscription> pauseSubscription(@PathVariable Long id) {
		return ResponseEntity.ok(subscriptionService.pauseSubscription(id));
	}

	@PatchMapping("/{id}/cancel")
	public ResponseEntity<Subscription> cancelSubscription(@PathVariable Long id) {
		return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
	}

	@PatchMapping("/{id}/reactivate")
	public ResponseEntity<Subscription> reactivateSubscription(@PathVariable Long id) {
		return ResponseEntity.ok(subscriptionService.reactivateSubscription(id));
	}
}
