package com.finance.tracker.model.entity;

import com.finance.tracker.model.IReportable;
import com.finance.tracker.model.enums.SubscriptionStatus;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
public class Subscription implements IReportable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

	@Embedded
	private BillingCycle billingCycle;

	public Subscription() {
	}

	public Subscription(Long id, String name, BigDecimal amount, SubscriptionStatus status, BillingCycle billingCycle) {
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.status = status;
		this.billingCycle = billingCycle;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public SubscriptionStatus getStatus() {
		return status;
	}

	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}

	public BillingCycle getBillingCycle() {
		return billingCycle;
	}

	public void setBillingCycle(BillingCycle billingCycle) {
		this.billingCycle = billingCycle;
	}

	public LocalDate getNextPaymentDate(LocalDate referenceDate) {
		if (billingCycle == null) {
			return null;
		}
		return billingCycle.calculateNextPaymentDate(referenceDate);
	}

	public boolean isRenewingIn(int days, LocalDate fromDate) {
		if (status != SubscriptionStatus.ACTIVE || billingCycle == null || days < 0 || fromDate == null) {
			return false;
		}

		LocalDate nextDate = getNextPaymentDate(fromDate);
		if (nextDate == null) {
			return false;
		}
		LocalDate upperBound = fromDate.plusDays(days);
		return !nextDate.isBefore(fromDate) && !nextDate.isAfter(upperBound);
	}

	public void pause() {
		if (status == SubscriptionStatus.CANCELLED) {
			throw new IllegalStateException("Cancelled subscription cannot be paused");
		}
		status = SubscriptionStatus.PAUSED;
	}

	public void cancel() {
		status = SubscriptionStatus.CANCELLED;
	}

	public void reactivate() {
		if (status == SubscriptionStatus.CANCELLED) {
			throw new IllegalStateException("Cancelled subscription cannot be reactivated");
		}
		status = SubscriptionStatus.ACTIVE;
	}

	@Override
	public String getReportLabel() {
		return name;
	}

	@Override
	public BigDecimal getReportAmount() {
		return amount;
	}

	@Override
	public LocalDate getReportDate() {
		return billingCycle == null ? null : billingCycle.getNextPaymentDate();
	}
}
