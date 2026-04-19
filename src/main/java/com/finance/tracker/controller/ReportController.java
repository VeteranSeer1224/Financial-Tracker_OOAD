package com.finance.tracker.controller;

import com.finance.tracker.infrastructure.export.IReportExporter;
import com.finance.tracker.model.IReportable;
import com.finance.tracker.model.entity.Subscription;
import com.finance.tracker.repository.SubscriptionRepository;
import com.finance.tracker.service.ReportService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final IReportExporter csvExporter;
    private final IReportExporter pdfExporter;
    private final SubscriptionRepository subscriptionRepository;

    public ReportController(ReportService reportService, 
                            @Qualifier("csvExporter") IReportExporter csvExporter,
                            @Qualifier("pdfExporter") IReportExporter pdfExporter,
                            SubscriptionRepository subscriptionRepository) {
        this.reportService = reportService;
        this.csvExporter = csvExporter;
        this.pdfExporter = pdfExporter;
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping("/export/{format}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String format) {
        
        List<IReportable> reportableData = new ArrayList<>();

        // --- MODULE 3 INTEGRATION (Subscriptions) ---
        // Fetch all real subscriptions from the database
        List<Subscription> subscriptions = (List<Subscription>) subscriptionRepository.findAll();
        reportableData.addAll(subscriptions);

        // --- MODULE 2 PLACEHOLDER (Expenses) ---
        // TODO: When Module 2 is pushed, inject ExpenseRepository into this controller
        // TODO: List<Expense> expenses = (List<Expense>) expenseRepository.findAll();
        // TODO: reportableData.addAll(expenses);
        
        // Generate the aggregated data using the real entities
        Map<String, Object> reportData = reportService.generateSummaryReport(reportableData);
        
        // Select the correct exporter based on the URL path
        IReportExporter exporter;
        if ("pdf".equalsIgnoreCase(format)) {
            exporter = pdfExporter;
        } else if ("csv".equalsIgnoreCase(format)) {
            exporter = csvExporter;
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Generate the file bytes
        byte[] fileData = exporter.exportReport(reportData);

        // Build the HTTP response headers so the browser downloads the file
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(exporter.getContentType()));
        
        String filename = "financial_report" + exporter.getFileExtension();
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}