package com.finance.tracker.repository;

import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.enums.SubscriptionStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserUserId(UUID userId);

    List<Subscription> findByUserUserIdAndStatus(UUID userId, SubscriptionStatus status);

    List<Subscription> findByStatusAndNextPaymentDateLessThanEqual(SubscriptionStatus status, LocalDate nextPaymentDate);

    Optional<Subscription> findBySubscriptionIdAndUserUserId(UUID subscriptionId, UUID userId);
}
