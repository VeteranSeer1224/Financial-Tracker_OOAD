package com.finance.tracker.model.enums;

import java.time.LocalDate;

public enum BillingFrequency {
	DAILY,
	WEEKLY,
	MONTHLY,
	QUARTERLY,
	YEARLY,
	CUSTOM_DAYS;

	public LocalDate addTo(LocalDate baseDate, int customIntervalDays) {
		if (baseDate == null) {
			throw new IllegalArgumentException("Base date cannot be null");
		}

		return switch (this) {
			case DAILY -> baseDate.plusDays(1);
			case WEEKLY -> baseDate.plusWeeks(1);
			case MONTHLY -> baseDate.plusMonths(1);
			case QUARTERLY -> baseDate.plusMonths(3);
			case YEARLY -> baseDate.plusYears(1);
			case CUSTOM_DAYS -> {
				if (customIntervalDays <= 0) {
					throw new IllegalArgumentException("Custom interval days must be greater than 0");
				}
				yield baseDate.plusDays(customIntervalDays);
			}
		};
	}
}
