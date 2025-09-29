package org.datn.bookstation.dto.response;

import java.math.BigDecimal;
import lombok.*;

/**
 * DTO dùng cho bán hàng tại quầy (POS).
 * Ánh xạ từ dữ liệu FE yêu cầu:
 * bookId, title/name, bookCode, quantity, unitPrice, originalPrice,
 * coverImageUrl, stockQuantity, isFlashSale, flashSaleItemId
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosBookItemResponse {
    private Integer bookId;
    private String title; // Có thể trùng name – giữ cả hai theo yêu cầu
    private String name;
    private String bookCode;
    private Integer quantity ; // Mặc định 1
    private BigDecimal unitPrice; // Giá dùng để tính tiền (flash sale hoặc thường)
    private BigDecimal originalPrice; // Giá gốc (normalPrice)
    private String coverImageUrl;
    private Integer stockQuantity;
    private Boolean isFlashSale ;
    private Integer flashSaleItemId; // null nếu không thuộc flash sale
}