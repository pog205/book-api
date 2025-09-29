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
public class RankStatisticsResponse {
    private List<RankUserCount> rankUserCounts;
    private List<RankAveragePoints> averagePointsByRank;
    private List<RankGrowthRate> monthlyGrowthRates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankUserCount {
        private String rankName;
        private Long userCount;
        private BigDecimal minSpent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankAveragePoints {
        private String rankName;
        private Double averagePoints;
        private BigDecimal minSpent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankGrowthRate {
        private String rankName;
        private Long currentMonthUsers;
        private Long previousMonthUsers;
        private Double growthRate;
    }
}
