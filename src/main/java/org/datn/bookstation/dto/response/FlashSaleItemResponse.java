package org.datn.bookstation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FlashSaleItemResponse {
    private Integer id;

    private Integer flashSaleId;
    private String flashSaleName;

    private Integer bookId;
    private String bookName;

    private BigDecimal discountPrice;
    private BigDecimal discountPercentage;

    private Integer stockQuantity;
    private Integer maxPurchasePerUser;

    private Byte status;
} 