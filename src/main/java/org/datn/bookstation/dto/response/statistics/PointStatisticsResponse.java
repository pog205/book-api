package org.datn.bookstation.dto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointStatisticsResponse {
    private Double averagePointsPerUser;
    private Long totalSystemPoints;
    private Long pointsEarnedThisMonth;
    private Long pointsSpentThisMonth;
    private List<TopPointEarner> topPointEarners;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPointEarner {
        private String fullName;
        private String email;
        private Integer totalPointsEarned;
        private String rankName;
    }
}
