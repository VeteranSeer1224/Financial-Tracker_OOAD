package com.finance.tracker.model;

import com.finance.tracker.model.enums.CategoryType;
import java.time.LocalDate;

public interface IReportable {
    double generateCost();

    CategoryType getCategory();

    LocalDate getTransactionDate();
}
