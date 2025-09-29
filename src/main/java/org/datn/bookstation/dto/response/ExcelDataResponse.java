package org.datn.bookstation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ExcelDataResponse {
    private List<Map<String, Object>> data;
    private Map<String, String> fields;
    private String fileName;
    private String sheetName;
    private Long totalRecords;
}
