package com.minip.financialtracker.service;

import com.minip.financialtracker.model.Budget;
import com.minip.financialtracker.model.Category;
import com.minip.financialtracker.model.Transaction;
import com.minip.financialtracker.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FinancialTracker {
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<Category, Budget> budgets = new EnumMap<>(Category.class);

    public Transaction addIncome(String description, BigDecimal amount, Category category, LocalDate date) {
        return addTransaction(TransactionType.INCOME, description, amount, category, date);
    }

    public Transaction addExpense(String description, BigDecimal amount, Category category, LocalDate date) {
        return addTransaction(TransactionType.EXPENSE, description, amount, category, date);
    }

    public void setBudget(Category category, BigDecimal limit) {
        budgets.put(Objects.requireNonNull(category, "category"), new Budget(category, limit));
    }

    public List<Transaction> getTransactions() {
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getDate).thenComparing(Transaction::getDescription))
                .toList();
    }

    public BigDecimal getBalance() {
        return transactions.stream()
                .map(Transaction::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public String getMonthlySummary(String monthInput) {
        YearMonth month = monthInput == null ? YearMonth.now() : parseMonth(monthInput);
        List<Transaction> monthlyTransactions = transactions.stream()
                .filter(transaction -> YearMonth.from(transaction.getDate()).equals(month))
                .sorted(Comparator.comparing(Transaction::getDate))
                .toList();

        BigDecimal income = sum(monthlyTransactions, TransactionType.INCOME);
        BigDecimal expense = sum(monthlyTransactions, TransactionType.EXPENSE);
        BigDecimal net = income.subtract(expense);

        StringBuilder summary = new StringBuilder();
        summary.append("Summary for ").append(month).append(System.lineSeparator());
        summary.append("Income: ").append(income.toPlainString()).append(System.lineSeparator());
        summary.append("Expense: ").append(expense.toPlainString()).append(System.lineSeparator());
        summary.append("Net: ").append(net.toPlainString()).append(System.lineSeparator());
        summary.append("Transactions:").append(System.lineSeparator());

        if (monthlyTransactions.isEmpty()) {
            summary.append("- none");
        } else {
            monthlyTransactions.forEach(transaction -> summary.append("- ")
                    .append(transaction.toDisplayString())
                    .append(System.lineSeparator()));
        }

        return summary.toString().trim();
    }

    public String getBudgetStatus() {
        if (budgets.isEmpty()) {
            return "";
        }

        YearMonth currentMonth = YearMonth.now();
        Collection<Transaction> monthlyExpenses = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .filter(transaction -> YearMonth.from(transaction.getDate()).equals(currentMonth))
                .toList();

        StringBuilder status = new StringBuilder();
        for (Budget budget : budgets.values()) {
            BigDecimal spent = monthlyExpenses.stream()
                    .filter(transaction -> transaction.getCategory() == budget.getCategory())
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remaining = budget.getLimit().subtract(spent);
            status.append(budget.getCategory())
                    .append(": spent ")
                    .append(spent.toPlainString())
                    .append(" / limit ")
                    .append(budget.getLimit().toPlainString())
                    .append(" -> ")
                    .append(remaining.signum() < 0 ? "over by " + remaining.abs().toPlainString() : "remaining " + remaining.toPlainString())
                    .append(System.lineSeparator());
        }
        return status.toString().trim();
    }

    private Transaction addTransaction(TransactionType type, String description, BigDecimal amount, Category category, LocalDate date) {
        Transaction transaction = new Transaction(type, description, amount, category, date);
        transactions.add(transaction);
        return transaction;
    }

    private static BigDecimal sum(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static YearMonth parseMonth(String input) {
        try {
            return YearMonth.parse(input);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Month must use YYYY-MM format", ex);
        }
    }
}
