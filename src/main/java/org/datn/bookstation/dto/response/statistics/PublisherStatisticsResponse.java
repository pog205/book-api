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
public class PublisherStatisticsResponse {
    private List<PublisherBookStatistic> bookStatistics;
    private List<PublisherRevenueStatistic> revenueStatistics;
    private List<TopPublisherByRevenue> topPublishersByRevenue;
    private List<TopPublisherByQuantity> topPublishersByQuantity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublisherBookStatistic {
        private String publisherName;
        private Long totalBooks;
        private Long newBooksThisMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublisherRevenueStatistic {
        private String publisherName;
        private BigDecimal totalRevenue;
        private Long totalQuantitySold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPublisherByRevenue {
        private String publisherName;
        private BigDecimal totalRevenue;
        private Long totalQuantitySold;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPublisherByQuantity {
        private String publisherName;
        private Long totalQuantitySold;
        private BigDecimal totalRevenue;
    }
}
