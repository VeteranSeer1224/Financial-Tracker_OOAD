package com.finance.tracker.repository;

import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
	List<Subscription> findByStatus(SubscriptionStatus status);
}
