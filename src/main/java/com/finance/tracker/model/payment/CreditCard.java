package com.finance.tracker.model.payment;

import jakarta.persistence.Entity;
import java.time.LocalDate;
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
public class CreditCard extends PaymentMethod {
    private String maskedCardNumber;
    private LocalDate expiryDate;

    @Override
    public boolean validate() {
        return maskedCardNumber != null
                && maskedCardNumber.matches("\\*{4,}\\d{4}")
                && expiryDate != null
                && !expiryDate.isBefore(LocalDate.now());
    }
}
