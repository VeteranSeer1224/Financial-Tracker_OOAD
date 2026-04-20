package com.finance.tracker.model.payment;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
public class DigitalWallet extends PaymentMethod {
    private String walletProvider;
    private String linkedEmail;

    @Override
    public boolean validate() {
        return walletProvider != null
                && !walletProvider.isBlank()
                && linkedEmail != null
                && linkedEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
