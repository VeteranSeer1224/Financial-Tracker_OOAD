package com.finance.tracker.service;

import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.exception.ValidationException;
import com.finance.tracker.model.entity.User;
import com.finance.tracker.model.payment.BankAccount;
import com.finance.tracker.model.payment.CreditCard;
import com.finance.tracker.model.payment.PaymentMethod;
import com.finance.tracker.repository.PaymentMethodRepository;
import com.finance.tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentManager {
    private final PaymentMethodRepository paymentRepo;
    private final UserRepository userRepository;

    @Transactional
    public PaymentMethod addPaymentMethod(UUID userId, PaymentMethod pm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        maskSensitiveData(pm);
        if (!pm.validate()) {
            throw new ValidationException("Invalid payment method payload");
        }

        pm.setUser(user);
        return paymentRepo.save(pm);
    }

    @Transactional
    public void removePaymentMethod(UUID userId, UUID pmId) {
        PaymentMethod paymentMethod = getPaymentMethodForUser(userId, pmId);
        paymentRepo.delete(paymentMethod);
    }

    public boolean processPayment(double amount, UUID pmId) {
        if (amount <= 0) {
            return false;
        }
        PaymentMethod paymentMethod = paymentRepo.findById(pmId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found: " + pmId));
        return paymentMethod.validate();
    }

    public List<PaymentMethod> getUserPaymentMethods(UUID userId) {
        return paymentRepo.findByUserUserId(userId);
    }

    public PaymentMethod getPaymentMethodForUser(UUID userId, UUID paymentMethodId) {
        PaymentMethod paymentMethod = paymentRepo.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found: " + paymentMethodId));
        if (paymentMethod.getUser() == null || !userId.equals(paymentMethod.getUser().getUserId())) {
            throw new ResourceNotFoundException("Payment method not associated with user: " + userId);
        }
        return paymentMethod;
    }

    private void maskSensitiveData(PaymentMethod pm) {
        if (pm instanceof CreditCard card) {
            card.setMaskedCardNumber(maskLastFour(card.getMaskedCardNumber()));
        }
        if (pm instanceof BankAccount account) {
            account.setMaskedAccountNumber(maskLastFour(account.getMaskedAccountNumber()));
        }
    }

    private String maskLastFour(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 4) {
            throw new ValidationException("Sensitive number must contain at least 4 digits");
        }
        return "****" + digits.substring(digits.length() - 4);
    }
}
