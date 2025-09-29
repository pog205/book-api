package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityRadarResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotTodayBook {
        private Long bookId;
        private String bookTitle;
        private String coverImageUrl;
        private Integer soldToday;
        private Integer currentStock;
        private String stockoutRisk; // "high", "medium", "low"
        private LocalDate estimatedStockoutDate;
        private Integer reorderSuggestion;
        private String actionUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendingBook {
        private Long bookId;
        private String bookTitle;
        private String coverImageUrl;
        private Integer socialMentions;
        private Double trendScore; // 0.0 - 1.0
        private Integer currentStock;
        private Integer suggestedOrder;
        private String trendSource;
        private String actionUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VipReturning {
        private Long userId;
        private String customerName;
        private String email;
        private LocalDate predictedReturnDate;
        private BigDecimal averageOrderValue;
        private List<String> preferredCategories;
        private Double confidence; // 0.0 - 1.0
        private String actionUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbandonedCart {
        private Long userId;
        private String customerName;
        private String email;
        private BigDecimal cartValue;
        private String cartAge;
        private Double riskScore; // 0.0 - 1.0
        private Integer suggestedDiscount;
        private String actionUrl;
    }

    private List<HotTodayBook> hotTodayBooks;
    private List<TrendingBook> trendingBooks;
    private List<VipReturning> vipReturningSoon;
    private List<AbandonedCart> abandonedCarts;
    private LocalDateTime lastUpdated;
}
