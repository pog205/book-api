package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Các DTO dùng cho dropdown select
 */
public class SelectOptions {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookOption {
        private Integer id;
        private String bookName;
        private String isbn;
        private String bookCode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserOption {
        private Integer id;
        private String email;
        private String phoneNumber;
        private String fullName;
    }
}
