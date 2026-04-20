package com.finance.tracker.infrastructure.export;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("csvExporter")
public class CsvExporter implements IReportExporter {
    @Override
    public byte[] export(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            builder.append(entry.getKey()).append(',').append(entry.getValue()).append('\n');
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
