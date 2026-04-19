package com.finance.tracker.infrastructure.export;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component("csvExporter")
public class CsvExporter implements IReportExporter {

    @Override
    public byte[] exportReport(Map<String, Object> reportData) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Metric,Value\n"); // CSV Header

        // Convert the map data into CSV rows
        for (Map.Entry<String, Object> entry : reportData.entrySet()) {
            csvBuilder.append(entry.getKey()).append(",")
                      .append(entry.getValue().toString()).append("\n");
        }

        return csvBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getFileExtension() {
        return ".csv";
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }
}