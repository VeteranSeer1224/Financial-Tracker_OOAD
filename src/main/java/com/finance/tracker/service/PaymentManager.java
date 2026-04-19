package com.finance.tracker.service;

import com.finance.tracker.model.payment.PaymentMethod;
import com.finance.tracker.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentManager {

    private final PaymentMethodRepository paymentRepository;

    @Autowired
    public PaymentManager(PaymentMethodRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Validates and adds a new payment method for a user.
     * @param userId The ID of the user.
     * @param pm The payment method to add.
     * @return The saved PaymentMethod object.
     */
    @Transactional
    public PaymentMethod addPaymentMethod(String userId, PaymentMethod pm) {
        pm.setUserId(userId);
        
        // Polymorphic validation (calls the specific validate method of CreditCard, BankAccount, etc.)
        if (!pm.validate()) {
            throw new IllegalArgumentException("Invalid payment method details provided.");
        }
        
        return paymentRepository.save(pm);
    }

    /**
     * Removes a payment method by its ID, ensuring it belongs to the specified user.
     * @param userId The ID of the user requesting the deletion.
     * @param pmId The ID of the payment method to remove.
     */
    @Transactional
    public void removePaymentMethod(String userId, String pmId) {
        Optional<PaymentMethod> pmOptional = paymentRepository.findById(pmId);
        
        if (pmOptional.isPresent() && pmOptional.get().getUserId().equals(userId)) {
            paymentRepository.deleteById(pmId);
        } else {
            throw new IllegalArgumentException("Payment method not found or does not belong to the user.");
        }
    }
    
    /**
     * Retrieves all payment methods for a given user.
     * @param userId The ID of the user.
     * @return A list of the user's payment methods.
     */
    public List<PaymentMethod> getUserPaymentMethods(String userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Simulates processing a payment. In a real application, this would integrate 
     * with an external gateway like Stripe or PayPal.
     * @param amount The amount to process.
     * @param pmId The ID of the payment method to use.
     * @return true if successful, false otherwise.
     */
    public boolean processPayment(double amount, String pmId) {
        Optional<PaymentMethod> pmOptional = paymentRepository.findById(pmId);
        
        if (pmOptional.isEmpty()) {
            return false;
        }
        
        // Simulated external gateway call
        System.out.println("Processing payment of $" + amount + " using method " + pmId);
        return true; 
    }
}
