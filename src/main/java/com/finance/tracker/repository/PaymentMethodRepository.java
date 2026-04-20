package com.finance.tracker.repository;

import com.finance.tracker.model.payment.PaymentMethod;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    List<PaymentMethod> findByUserUserId(UUID userId);
}
