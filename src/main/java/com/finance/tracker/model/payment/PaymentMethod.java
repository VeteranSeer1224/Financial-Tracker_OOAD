package com.finance.tracker.model.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.finance.tracker.model.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreditCard.class, name = "CREDIT_CARD"),
        @JsonSubTypes.Type(value = BankAccount.class, name = "BANK_ACCOUNT"),
        @JsonSubTypes.Type(value = DigitalWallet.class, name = "DIGITAL_WALLET")
})
public abstract class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID paymentMethodId;

    protected String holderName;
    protected boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    protected User user;

    public abstract boolean validate();
}
