package com.finance.tracker.service;

import com.finance.tracker.model.IReportable;
import com.finance.tracker.model.enums.CategoryType;
import com.finance.tracker.infrastructure.export.IReportExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final IReportExporter csvExporter;


    public ReportService(@Qualifier("csvExporter") IReportExporter csvExporter) {
        this.csvExporter = csvExporter;
    }

    /**
     * Groups transactions by their category and sums the amounts.
     */
    public Map<CategoryType, BigDecimal> getCategoryBreakdown(List<IReportable> transactions) {
        return transactions.stream()
                // Filter out any malformed data just to be safe
                .filter(tx -> tx.getCategoryType() != null && tx.getAmount() != null)
                // Group by CategoryType, and reduce (sum) the BigDecimals
                .collect(Collectors.groupingBy(
                        IReportable::getCategoryType,
                        Collectors.reducing(BigDecimal.ZERO, IReportable::getAmount, BigDecimal::add)
                ));
    }

    /**
     * Generates a flat map of metrics for the exporters to read.
     */
    public Map<String, Object> generateSummaryReport(List<IReportable> transactions) {
        Map<String, Object> report = new HashMap<>();
        
        // 1. Calculate Grand Total
        BigDecimal totalSpent = transactions.stream()
                .map(tx -> tx.getAmount() != null ? tx.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.put("Total Transactions", transactions.size());
        report.put("Total Spent", totalSpent);

        Map<CategoryType, BigDecimal> breakdown = getCategoryBreakdown(transactions);
        for (Map.Entry<CategoryType, BigDecimal> entry : breakdown.entrySet()) {
            report.put("Category: " + entry.getKey().name(), entry.getValue());
        }

        return report;
    }

    public byte[] exportSummaryReport(List<IReportable> transactions) {
        Map<String, Object> reportData = generateSummaryReport(transactions);
        return csvExporter.exportReport(reportData);
    }
}