package com.finance.tracker.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.tracker.model.IReportable;
import com.finance.tracker.model.enums.CategoryType;
import com.finance.tracker.model.payment.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Expense implements IReportable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID expenseId;

    private double amount;
    private LocalDate date;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    public boolean validate() {
        return amount > 0 && date != null && !date.isAfter(LocalDate.now()) && description != null && !description.isBlank();
    }

    @Override
    public double generateCost() {
        return amount;
    }

    @Override
    public CategoryType getCategory() {
        return category == null ? CategoryType.OTHER : category.getType();
    }

    @Override
    public LocalDate getTransactionDate() {
        return date;
    }

    @JsonProperty("paymentMethodId")
    public UUID getPaymentMethodId() {
        return paymentMethod == null ? null : paymentMethod.getPaymentMethodId();
    }

    @JsonProperty("paymentMethodType")
    public String getPaymentMethodType() {
        return paymentMethod == null ? null : Hibernate.getClass(paymentMethod).getSimpleName();
    }

    @JsonIgnore
    public Category getCategoryEntity() {
        return category;
    }
}
