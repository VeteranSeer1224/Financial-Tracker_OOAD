package com.minip.financialtracker.service;

import com.minip.financialtracker.model.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialTrackerTest {

    @Test
    void balanceReflectsIncomeAndExpenses() {
        FinancialTracker tracker = new FinancialTracker();
        tracker.addIncome("Salary", new BigDecimal("1000"), Category.SALARY, LocalDate.of(2026, 4, 1));
        tracker.addExpense("Food", new BigDecimal("250"), Category.FOOD, LocalDate.of(2026, 4, 2));

        assertEquals(0, new BigDecimal("750.00").compareTo(tracker.getBalance()));
    }

    @Test
    void monthlySummaryIncludesTransactions() {
        FinancialTracker tracker = new FinancialTracker();
        tracker.addIncome("Salary", new BigDecimal("1000"), Category.SALARY, LocalDate.of(2026, 4, 1));

        String summary = tracker.getMonthlySummary("2026-04");

        assertTrue(summary.contains("Summary for 2026-04"));
        assertTrue(summary.contains("Salary"));
    }

    @Test
    void budgetStatusShowsRemainingAmount() {
        FinancialTracker tracker = new FinancialTracker();
        tracker.setBudget(Category.FOOD, new BigDecimal("300"));
        tracker.addExpense("Groceries", new BigDecimal("120"), Category.FOOD, LocalDate.now());

        String status = tracker.getBudgetStatus();

        assertTrue(status.contains("FOOD"));
        assertTrue(status.contains("remaining"));
    }
}
