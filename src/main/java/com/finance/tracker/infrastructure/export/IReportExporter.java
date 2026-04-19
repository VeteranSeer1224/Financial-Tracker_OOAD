package com.finance.tracker.infrastructure.export;

import java.util.Map;

public interface IReportExporter {
    // Returns a byte array representing the file (PDF/CSV) to be downloaded
    byte[] exportReport(Map<String, Object> reportData);
    String getFileExtension();
    String getContentType();
}