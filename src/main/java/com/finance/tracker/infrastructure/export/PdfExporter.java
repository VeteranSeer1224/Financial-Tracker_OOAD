package com.finance.tracker.infrastructure.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("pdfExporter")
public class PdfExporter implements IReportExporter {
    @Override
    public byte[] export(Map<String, Object> data) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Financial Report Export", titleFont));
            document.add(new Paragraph(
                    "Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), bodyFont));
            document.add(new Paragraph(" "));

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                document.add(new Paragraph(entry.getKey() + ": " + String.valueOf(entry.getValue()), bodyFont));
            }

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Unexpected error during PDF export", e);
        }
    }
}
