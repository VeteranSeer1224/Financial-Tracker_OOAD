package com.finance.tracker.infrastructure.export;

import java.util.Map;

public interface IReportExporter {
    byte[] export(Map<String, Object> data);
}
