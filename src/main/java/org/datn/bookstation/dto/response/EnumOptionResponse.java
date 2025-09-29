package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnumOptionResponse {
    private String value;      // Giá trị enum (BOOK_LAUNCH, DRAFT, ...)
    private String displayName; // Tên hiển thị (Sự kiện ra mắt sách mới, Bản nháp, ...)
}
