package com.finance.tracker.repository;

import com.finance.tracker.model.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {
    
    /**
     * Retrieves all payment methods associated with a specific user.
     * * @param userId The ID of the user.
     * @return A list of PaymentMethod objects (can be CreditCard, BankAccount, or DigitalWallet).
     */
    List<PaymentMethod> findByUserId(String userId);
}
