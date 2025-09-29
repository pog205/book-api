package org.datn.bookstation.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlashSaleStatsResponse {
    private long totalFlashSales;
    private long totalFlashSaleOrders;
    private long activeFlashSales;
    private String bestSellingFlashSaleBookName;
}