package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MultipleUploadResponse {
    private boolean success;
    private List<String> urls;
    private String message;
}
