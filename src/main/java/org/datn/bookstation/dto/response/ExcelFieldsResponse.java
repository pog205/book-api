package org.datn.bookstation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ExcelFieldsResponse {
    private Map<String, String> fields;
    private String entityType;
}
