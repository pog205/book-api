package org.datn.bookstation.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleItemStatsResponse {
    private long totalBooksInFlashSale;
    private long totalBooksSoldInFlashSale;
    private String topSellingBookName;
    private long totalFlashSaleStock;
}