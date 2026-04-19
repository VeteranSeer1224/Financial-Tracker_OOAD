package com.finance.tracker.service;

import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.enums.SubscriptionStatus;
import com.finance.tracker.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {
	private final SubscriptionRepository subscriptionRepository;
	private final Clock clock;

	@Autowired
	public SubscriptionService(SubscriptionRepository subscriptionRepository) {
		this(subscriptionRepository, Clock.systemDefaultZone());
	}

	SubscriptionService(SubscriptionRepository subscriptionRepository, Clock clock) {
		this.subscriptionRepository = subscriptionRepository;
		this.clock = clock;
	}

	public List<Subscription> getSubscriptionsRenewingIn(int days) {
		if (days < 0) {
			throw new IllegalArgumentException("Days cannot be negative");
		}

		LocalDate today = LocalDate.now(clock);
		return subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE)
				.stream()
				.filter(subscription -> subscription.isRenewingIn(days, today))
				.toList();
	}

	public Subscription pauseSubscription(Long id) {
		Subscription subscription = findByIdOrThrow(id);
		subscription.pause();
		return subscriptionRepository.save(subscription);
	}

	public Subscription cancelSubscription(Long id) {
		Subscription subscription = findByIdOrThrow(id);
		subscription.cancel();
		return subscriptionRepository.save(subscription);
	}

	public Subscription reactivateSubscription(Long id) {
		Subscription subscription = findByIdOrThrow(id);
		subscription.reactivate();
		return subscriptionRepository.save(subscription);
	}

	private Subscription findByIdOrThrow(Long id) {
		return subscriptionRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));
	}
}
