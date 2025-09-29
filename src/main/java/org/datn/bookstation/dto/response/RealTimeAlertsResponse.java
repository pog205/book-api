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
public class RealTimeAlertsResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Alert {
        private String id;
        private String type; // "COD_UNCONFIRMED", "LOW_INVENTORY", "REVENUE_DROP", "TREND_OPPORTUNITY"
        private String severity; // "high", "medium", "low"
        private String title;
        private String message;
        private Integer affectedCount;
        private BigDecimal totalValue;
        private Long bookId;
        private Integer currentStock;
        private Integer dailyAvgSales;
        private String stockoutEstimate;
        private String region;
        private Double dropPercentage;
        private Integer socialMentions;
        private String actionUrl;
        private LocalDateTime createdAt;
        private Integer priority; // 1-5
        private boolean isResolved;
        private LocalDateTime resolvedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertStats {
        private Integer totalActive;
        private Integer critical;
        private Integer warning;
        private Integer info;
        private Integer resolved24h;
    }

    private List<Alert> criticalAlerts;
    private List<Alert> warningAlerts;
    private List<Alert> infoAlerts;
    private AlertStats alertStats;
    private LocalDateTime lastUpdated;
}
