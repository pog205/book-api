package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleUploadResponse {
    private boolean success;
    private String url;
    private String message;
}
