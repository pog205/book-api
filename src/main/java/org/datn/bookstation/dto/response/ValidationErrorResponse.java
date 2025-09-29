package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResponse {
    private String field;
    private String message;
    private Object rejectedValue;
    
    public ValidationErrorResponse(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
