package com.finance.tracker.model.entity;

import com.finance.tracker.model.enums.BillingFrequency;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDate;

@Embeddable
public class BillingCycle {
	@Enumerated(EnumType.STRING)
	private BillingFrequency frequency;
	private Integer customIntervalDays;
	private LocalDate nextPaymentDate;

	public BillingCycle() {
	}

	public BillingCycle(BillingFrequency frequency, Integer customIntervalDays, LocalDate nextPaymentDate) {
		this.frequency = frequency;
		this.customIntervalDays = customIntervalDays;
		this.nextPaymentDate = nextPaymentDate;
		validate();
	}

	public BillingFrequency getFrequency() {
		return frequency;
	}

	public void setFrequency(BillingFrequency frequency) {
		this.frequency = frequency;
		validate();
	}

	public Integer getCustomIntervalDays() {
		return customIntervalDays;
	}

	public void setCustomIntervalDays(Integer customIntervalDays) {
		this.customIntervalDays = customIntervalDays;
		validate();
	}

	public LocalDate getNextPaymentDate() {
		return nextPaymentDate;
	}

	public void setNextPaymentDate(LocalDate nextPaymentDate) {
		this.nextPaymentDate = nextPaymentDate;
	}

	public LocalDate calculateNextPaymentDate(LocalDate referenceDate) {
		validate();
		if (referenceDate == null) {
			throw new IllegalArgumentException("Reference date cannot be null");
		}
		if (nextPaymentDate == null) {
			throw new IllegalStateException("Next payment date must be initialized");
		}

		LocalDate candidate = nextPaymentDate;
		while (!candidate.isAfter(referenceDate)) {
			candidate = frequency.addTo(candidate, customIntervalDays == null ? 0 : customIntervalDays);
		}
		return candidate;
	}

	public LocalDate rollForwardFrom(LocalDate referenceDate) {
		LocalDate recalculated = calculateNextPaymentDate(referenceDate);
		this.nextPaymentDate = recalculated;
		return recalculated;
	}

	private void validate() {
		if (frequency == null) {
			return;
		}

		if (frequency == BillingFrequency.CUSTOM_DAYS) {
			if (customIntervalDays == null || customIntervalDays <= 0) {
				throw new IllegalArgumentException("Custom interval days must be greater than 0 for CUSTOM_DAYS");
			}
		} else {
			customIntervalDays = null;
		}
	}
}
