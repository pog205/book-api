package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSalesChartResponse {
    private String chartType; // "daily", "weekly", "monthly"
    private List<SalesDataPoint> dataPoints;
    private SalesSummary summary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesDataPoint {
        private Long timestamp;
        private String label; // "2024-01-15", "Week 3", "January 2024"
        private BigDecimal totalRevenue;
        private Long totalBooksSold;
        private Integer uniqueBookCount;
        private BigDecimal averageOrderValue;
        private Double growthRate; // % compared to previous period
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesSummary {
        private BigDecimal totalRevenue;
        private Long totalBooksSold;
        private Integer totalDataPoints;
        private BigDecimal averageRevenuePerPeriod;
        private Double overallGrowthRate;
        private String trendDirection; // "UP", "DOWN", "STABLE"
        private String performanceLevel; // "EXCELLENT", "GOOD", "AVERAGE", "POOR"
    }
}
