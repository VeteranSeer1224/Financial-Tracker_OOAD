package com.finance.tracker.model.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bank_accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BankAccount extends PaymentMethod {

    @Column(nullable = false)
    private String routingNumber;

    @Column(nullable = false)
    private String accountNumber;

    @Override
    public boolean validate() {
        // Basic validation: Routing number is typically 9 digits
        if (routingNumber == null || !routingNumber.matches("\\d{9}")) {
            return false;
        }
        
        // Account number must exist and contain only digits (typically between 4 and 17 digits)
        if (accountNumber == null || !accountNumber.matches("\\d{4,17}")) {
            return false;
        }
        
        return true;
    }
}
