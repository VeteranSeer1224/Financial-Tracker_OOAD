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
public class BankAccount extends PaymentMethod {
    private String routingNumber;
    private String maskedAccountNumber;

    @Override
    public boolean validate() {
        return routingNumber != null
                && routingNumber.matches("\\d{9}")
                && maskedAccountNumber != null
                && maskedAccountNumber.matches("\\*{4,}\\d{4}");
    }
}
