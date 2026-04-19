package com.finance.tracker.model.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "credit_cards")
@Data
@EqualsAndHashCode(callSuper = true) // Ensures Lombok includes the parent class fields in equals/hashCode
@NoArgsConstructor
public class CreditCard extends PaymentMethod {

    @Column(nullable = false)
    private String cardNumber; 

    @Column(nullable = false)
    private Date expiryDate;

    @Override
    public boolean validate() {
        // Basic validation: card number must exist and have at least 13 digits (standard minimum)
        if (cardNumber == null || cardNumber.trim().length() < 13) {
            return false;
        }
        
        // Expiry date must not be in the past
        if (expiryDate == null || expiryDate.before(new Date())) {
            return false;
        }
        
        return true;
    }
}
