package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationStatsResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private String provinceName;
        private Integer provinceId;
        private Long orderCount;
        private BigDecimal totalAmount;
        private Double percentage;
        // For heatmap
        private Double intensity; // 0.0 to 1.0
    }
    
    private List<LocationData> provinces;
    private Long totalOrders;
    private BigDecimal totalAmount;
}
