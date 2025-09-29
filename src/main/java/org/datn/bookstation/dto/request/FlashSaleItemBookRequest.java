package org.datn.bookstation.dto.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlashSaleItemBookRequest {
    private Integer id;
    private String bookName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer discountPercentage;
    private Integer stockQuantity;
    private List<String> images;
    private String categoryName;
    private Boolean isInFlashSale;
    private BigDecimal flashSalePrice;
    private Integer flashSaleStockQuantity;
    private Integer flashSaleSoldCount;
    private Integer soldCount;
    // Trạng thái giảm giá có đang kích hoạt không
    private Boolean discountActive;



}
