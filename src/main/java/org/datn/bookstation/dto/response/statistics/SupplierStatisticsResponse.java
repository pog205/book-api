package org.datn.bookstation.dto.response.statistics;

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
public class SupplierStatisticsResponse {
    private List<SupplierBookStatistic> bookStatistics;
    private List<SupplierRevenueStatistic> revenueStatistics;
    private List<TopSupplierByRevenue> topSuppliersByRevenue;
    private List<TopSupplierByQuantity> topSuppliersByQuantity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierBookStatistic {
        private String supplierName;
        private Long totalBooks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierRevenueStatistic {
        private String supplierName;
        private BigDecimal totalRevenue;
        private Long totalQuantitySold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSupplierByRevenue {
        private String supplierName;
        private BigDecimal totalRevenue;
        private Long totalQuantitySold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSupplierByQuantity {
        private String supplierName;
        private Long totalQuantitySold;
        private BigDecimal totalRevenue;
    }
}
