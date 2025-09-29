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
public class CrossSellSuggestionResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuggestedProduct {
        private Integer bookId;
        private String bookTitle;
        private String bookImage;
        private String authorName;
        private java.math.BigDecimal price;
        private Double confidence; // 0.0 to 1.0
        private String reason; // "Customers who bought this also bought", "Same author", "Same category"
    }
    
    private Integer currentOrderId;
    private List<SuggestedProduct> suggestedProducts;
    private String suggestionType; // "upsell", "cross_sell"
}
