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
public class OrderHealthMapResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionHealth {
        private String provinceName;
        private Integer provinceId;
        private Double healthScore; // 0.0 - 1.0
        private Double orderGrowth;
        private Double deliverySuccessRate;
        private String status; // "excellent", "good", "concerning", "critical"
        private String alertLevel; // "green", "yellow", "red"
        private String actionSuggestion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingPartner {
        private String partnerName;
        private Double successRate;
        private Double avgDeliveryTime; // in days
        private List<String> failureReasons;
        private String alertLevel;
        private Integer totalOrders;
        private Integer successfulOrders;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskOrder {
        private Long orderId;
        private String customerName;
        private String customerPhone;
        private String riskType; // "COD_BOMB", "DELAYED_SHIPPING", "MULTIPLE_ATTEMPTS"
        private Double riskScore; // 0.0 - 1.0
        private Integer daysOverdue;
        private BigDecimal orderValue;
        private LocalDate expectedDelivery;
        private Integer currentDelay; // in days
        private String actionUrl;
        private String riskReason;
    }

    private List<RegionHealth> regionHealth;
    private List<ShippingPartner> shippingPartners;
    private List<RiskOrder> riskOrders;
    private Double overallHealthScore;
    private String healthStatus;
    private LocalDateTime lastUpdated;
}
