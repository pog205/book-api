package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesData {
        private Integer bookId;
        private String bookTitle;
        private String bookImage;
        private String authorName;
        private Long quantitySold;
        private java.math.BigDecimal totalRevenue;
    }
    
    private List<ProductSalesData> topProducts;
    private String period; // "today", "week", "month"
}
