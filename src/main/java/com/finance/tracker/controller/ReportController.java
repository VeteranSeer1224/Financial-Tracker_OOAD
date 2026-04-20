package com.finance.tracker.controller;

import com.finance.tracker.service.ReportService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/{userId}/reports")
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/monthly")
    public Map<String, Object> monthly(@PathVariable UUID userId, @RequestParam int month, @RequestParam int year) {
        return reportService.generateMonthlyReport(userId, month, year);
    }

    @GetMapping("/annual")
    public Map<String, Object> annual(@PathVariable UUID userId, @RequestParam int year) {
        return reportService.generateAnnualReport(userId, year);
    }

    @GetMapping("/optimization")
    public Map<String, Object> optimization(@PathVariable UUID userId) {
        return reportService.generateOptimizationReport(userId);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @PathVariable UUID userId,
            @RequestParam String type,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "csv") String format) {
        Map<String, Object> data = switch (type.toLowerCase()) {
            case "monthly" -> reportService.generateMonthlyReport(userId, month, year);
            case "annual" -> reportService.generateAnnualReport(userId, year);
            case "optimization" -> reportService.generateOptimizationReport(userId);
            default -> throw new IllegalArgumentException("Unknown report type: " + type);
        };
        byte[] content = reportService.exportReport(data, format);
        MediaType mediaType = "pdf".equalsIgnoreCase(format) ? MediaType.APPLICATION_PDF : MediaType.TEXT_PLAIN;
        String filename = type + "-report." + format.toLowerCase();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(mediaType)
                .body(content);
    }
}
