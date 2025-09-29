package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho API search books dropdown - CHỈ TÊN, ID, MÃ SÁCH
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResponse {
    
    private Integer bookId;
    private String bookName;
    private String isbn;          // Mã sách
    private String imageUrl;      // Thêm ảnh để hiển thị đẹp hơn
    
}
