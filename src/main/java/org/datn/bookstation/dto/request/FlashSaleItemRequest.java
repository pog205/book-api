package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlashSaleItemRequest {
    @NotNull
    private Integer flashSaleId;

    @NotNull
    private Integer bookId;

    @NotNull
    private BigDecimal discountPrice;

    @NotNull
    private BigDecimal discountPercentage;

    @NotNull
    private Integer stockQuantity;

    private Integer maxPurchasePerUser;

    private Byte status; // 1 active, 0 inactive (optional)

    private Integer createdBy;

    private Integer updatedBy;
} 