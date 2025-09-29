package org.datn.bookstation.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookPriceCalculationResponse {
    private Integer bookId;
    private String bookName;
    private BigDecimal originalPrice;        // Giá gốc
    private BigDecimal finalPrice;          // Giá cuối cùng sau discount
    private BigDecimal discountAmount;      // Số tiền giảm
    private Integer discountPercent;        // % giảm giá thực tế
    private Boolean hasDiscount;            // Có áp dụng discount hay không
    private String discountType;            // "VALUE" hoặc "PERCENT"
    
    // Flash sale info (nếu có)
    private Boolean hasFlashSale;           // Có flash sale đang active không
    private BigDecimal flashSalePrice;     // Giá flash sale
    private BigDecimal flashSavings;       // Số tiền tiết kiệm từ flash sale
    private String flashSaleName;          // Tên flash sale
}
