package com.finance.tracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IReportable {
	String getReportLabel();

	BigDecimal getReportAmount();

	LocalDate getReportDate();
}
