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
public class SurvivalKpiResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyOrderMetric {
        private Integer today;
        private Integer yesterday;
        private Double growthRate;
        private String trend; // "up", "down", "stable"
        private String alertLevel; // "green", "warning", "critical"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyRevenueMetric {
        private BigDecimal thisWeek;
        private BigDecimal lastWeek;
        private Double growthRate;
        private String trend;
        private String alertLevel;
        private String actionSuggestion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundCancelMetric {
        private Double rate;
        private Double threshold;
        private String alertLevel;
        private Integer totalRefundCancel;
        private Integer totalOrders;
        private String actionSuggestion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetentionMetric {
        private Double rate;
        private Double previousRate;
        private String trend;
        private Integer returningCustomers;
        private Integer totalCustomers;
        private String alertLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstantAction {
        private String priority; // "high", "medium", "low"
        private String type;
        private String message;
        private String actionUrl;
        private String estimatedImpact;
    }

    private DailyOrderMetric dailyOrders;
    private WeeklyRevenueMetric weeklyRevenue;
    private RefundCancelMetric refundCancelRate;
    private RetentionMetric retentionRate;
    private List<InstantAction> instantActions;
    private LocalDateTime lastUpdated;
}
