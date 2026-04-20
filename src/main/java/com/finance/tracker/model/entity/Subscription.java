package com.finance.tracker.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.tracker.model.IReportable;
import com.finance.tracker.model.enums.CategoryType;
import com.finance.tracker.model.enums.SubscriptionStatus;
import com.finance.tracker.model.payment.PaymentMethod;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
public class Subscription implements IReportable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID subscriptionId;

    private String serviceName;
    private double cost;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDate lastAccessDate;
    private LocalDate nextPaymentDate;

    @Embedded
    private BillingCycle billingCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;

    public void pause() {
        this.status = SubscriptionStatus.PAUSED;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public void reactivate() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    @Override
    public double generateCost() {
        return cost;
    }

    @Override
    public CategoryType getCategory() {
        return category == null ? CategoryType.OTHER : category.getType();
    }

    @Override
    public LocalDate getTransactionDate() {
        return nextPaymentDate;
    }

    @PrePersist
    @PreUpdate
    public void updateNextPaymentDate() {
        if (billingCycle != null) {
            this.nextPaymentDate = billingCycle.getNextPaymentDate();
        }
    }

    @JsonProperty("paymentMethodId")
    public UUID getPaymentMethodId() {
        return paymentMethod == null ? null : paymentMethod.getPaymentMethodId();
    }

    @JsonProperty("paymentMethodType")
    public String getPaymentMethodType() {
        return paymentMethod == null ? null : Hibernate.getClass(paymentMethod).getSimpleName();
    }
}
