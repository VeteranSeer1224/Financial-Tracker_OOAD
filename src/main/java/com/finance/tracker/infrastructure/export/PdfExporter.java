package com.finance.tracker.infrastructure.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Component("pdfExporter")
public class PdfExporter implements IReportExporter {

    @Override
    public byte[] exportReport(Map<String, Object> reportData) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            // Bind the document to our output stream
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Add a Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Paragraph title = new Paragraph("Financial Summary Report", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 2. Create a Table with 2 columns
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            
            // Add Table Headers
            table.addCell("Metric");
            table.addCell("Value");

            // 3. Populate Table Rows with our Data
            for (Map.Entry<String, Object> entry : reportData.entrySet()) {
                table.addCell(entry.getKey());
                table.addCell(entry.getValue() != null ? entry.getValue().toString() : "");
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF document", e);
        }

        return out.toByteArray(); // Return the constructed PDF as raw bytes
    }

    @Override
    public String getFileExtension() {
        return ".pdf";
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }
}