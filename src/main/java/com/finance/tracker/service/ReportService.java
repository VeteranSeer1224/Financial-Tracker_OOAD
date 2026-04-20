package com.finance.tracker.service;

import com.finance.tracker.infrastructure.export.IReportExporter;
import com.finance.tracker.model.entity.Expense;
import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.model.enums.CategoryType;
import com.finance.tracker.model.enums.SubscriptionStatus;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ExpenseRepository expenseRepository;
    private final SubscriptionRepository subscriptionRepository;
    @Qualifier("csvExporter")
    private final IReportExporter csvExporter;
    @Qualifier("pdfExporter")
    private final IReportExporter pdfExporter;

    public Map<String, Object> generateMonthlyReport(UUID userId, int month, int year) {
        return generateMonthlyReport(userId, month, year, null);
    }

    public Map<String, Object> generateMonthlyReport(UUID userId, int month, int year, List<CategoryType> categories) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        List<Expense> expenses = applyCategoryFilter(expenseRepository.findByUserUserIdAndDateBetween(userId, from, to), categories);
        List<Subscription> subscriptions =
                subscriptionRepository.findByUserUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).stream()
                        .filter(subscription -> subscription.getNextPaymentDate() != null
                                && !subscription.getNextPaymentDate().isBefore(from)
                                && !subscription.getNextPaymentDate().isAfter(to))
                        .toList();

        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "MONTHLY");
        report.put("period", from + " to " + to);
        report.put("categoryBreakdown", getCategoryBreakdown(expenses));
        report.put("totalSubscriptionCost", subscriptions.stream().mapToDouble(Subscription::getCost).sum());
        report.put("totalExpenseCost", expenses.stream().mapToDouble(Expense::getAmount).sum());
        report.put("expensesByPaymentMethod",
                expenses.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getPaymentMethod() == null
                                        ? "UNKNOWN"
                                        : Hibernate.getClass(e.getPaymentMethod()).getSimpleName(),
                                Collectors.summingDouble(Expense::getAmount))));
        return report;
    }

    public Map<String, Object> generateAnnualReport(UUID userId, int year) {
        return generateAnnualReport(userId, year, null);
    }

    public Map<String, Object> generateAnnualReport(UUID userId, int year, List<CategoryType> categories) {
        List<Map<String, Object>> months = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            months.add(generateMonthlyReport(userId, month, year, categories));
        }
        double totalAnnualExpenses = months.stream()
                .mapToDouble(month -> ((Number) month.getOrDefault("totalExpenseCost", 0)).doubleValue())
                .sum();
        double totalAnnualSubscriptions = months.stream()
                .mapToDouble(month -> ((Number) month.getOrDefault("totalSubscriptionCost", 0)).doubleValue())
                .sum();
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "ANNUAL");
        report.put("year", year);
        report.put("monthlyReports", months);
        report.put(
                "summary",
                Map.of(
                        "totalAnnualExpenses", totalAnnualExpenses,
                        "totalAnnualSubscriptions", totalAnnualSubscriptions,
                        "grandTotal", totalAnnualExpenses + totalAnnualSubscriptions));
        return report;
    }

    public Map<String, Object> generateOptimizationReport(UUID userId) {
        return generateOptimizationReport(userId, null);
    }

    public Map<String, Object> generateOptimizationReport(UUID userId, List<CategoryType> categories) {
        List<Expense> filteredExpenses = applyCategoryFilter(expenseRepository.findByUserUserId(userId), categories);
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "OPTIMIZATION");
        report.put(
                "topExpenses",
                filteredExpenses.stream()
                        .sorted(Comparator.comparingDouble(Expense::getAmount).reversed())
                        .limit(10)
                        .toList());
        report.put(
                "unusedSubscriptions",
                identifyUnusedSubscriptions(userId).stream()
                        .map(subscription -> Map.of("subscription", subscription, "considerCancelling", true))
                        .toList());
        return report;
    }

    public byte[] exportReport(Map<String, Object> data, String format) {
        if ("pdf".equalsIgnoreCase(format)) {
            return pdfExporter.export(data);
        }
        return csvExporter.export(data);
    }

    public Map<CategoryType, Double> getCategoryBreakdown(List<Expense> expenses) {
        return expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));
    }

    public double getTotalSubscriptionCost(UUID userId) {
        return subscriptionRepository.findByUserUserId(userId).stream().mapToDouble(Subscription::getCost).sum();
    }

    public List<Expense> getTopExpenses(UUID userId, int limit) {
        return expenseRepository.findByUserUserId(userId).stream()
                .sorted(Comparator.comparingDouble(Expense::getAmount).reversed())
                .limit(limit)
                .toList();
    }

    public List<Subscription> identifyUnusedSubscriptions(UUID userId) {
        LocalDate threshold = LocalDate.now().minusDays(30);
        return subscriptionRepository.findByUserUserIdAndStatus(userId, SubscriptionStatus.ACTIVE).stream()
                .filter(sub -> sub.getLastAccessDate() == null || sub.getLastAccessDate().isBefore(threshold))
                .toList();
    }

    private List<Expense> applyCategoryFilter(List<Expense> expenses, List<CategoryType> categories) {
        if (categories == null || categories.isEmpty()) {
            return expenses;
        }
        return expenses.stream()
                .filter(expense -> categories.contains(expense.getCategory()))
                .toList();
    }
}
