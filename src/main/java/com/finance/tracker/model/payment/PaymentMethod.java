package com.finance.tracker.model.payment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_methods")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, 
        include = JsonTypeInfo.As.PROPERTY, 
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreditCard.class, name = "CREDIT_CARD"),
        @JsonSubTypes.Type(value = BankAccount.class, name = "BANK_ACCOUNT"),
        @JsonSubTypes.Type(value = DigitalWallet.class, name = "DIGITAL_WALLET")
})
public abstract class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String paymentMethodId;

    @Column(nullable = false)
    protected String holderName;

    @Column(nullable = false)
    protected boolean isDefault = false;

    // A foreign key reference to associate the payment method with its owner
    @Column(name = "user_id", nullable = false)
    protected String userId;

    /**
     * Abstract validation method to be implemented by all specific payment types.
     * Ensures each method has its own business rules for validity.
     */
    public abstract boolean validate();
}
