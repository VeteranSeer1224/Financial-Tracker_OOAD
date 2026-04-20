package com.finance.tracker.infrastructure.export;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("pdfExporter")
public class PdfExporter implements IReportExporter {
    @Override
    public byte[] export(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder("PDF Export\n");
        data.forEach((k, v) -> builder.append(k).append(": ").append(v).append('\n'));
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
