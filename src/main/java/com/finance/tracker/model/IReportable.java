package com.finance.tracker.model;

import com.finance.tracker.model.enums.CategoryType;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface IReportable {
    BigDecimal getAmount();
    LocalDate getTransactionDate();
    CategoryType getCategoryType();
    String getDescription();
}