package com.finance.tracker.model.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "digital_wallets")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DigitalWallet extends PaymentMethod {

    @Column(nullable = false)
    private String walletProvider; // e.g., "PayPal", "Apple Pay"

    @Column(nullable = false)
    private String linkedEmail;

    @Override
    public boolean validate() {
        // Provider must be explicitly declared
        if (walletProvider == null || walletProvider.trim().isEmpty()) {
            return false;
        }
        
        // Linked email must match a basic email regex pattern
        if (linkedEmail == null || !linkedEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return false;
        }
        
        return true;
    }
}
