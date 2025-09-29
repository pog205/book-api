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
public class UserStatisticsResponse {
    private Long totalUsers;
    private Long newUsersThisMonth;
    private Long newUsersThisWeek;
    private Long activeUsers;
    private Double activityRate;
    private List<UserRankStatistic> usersByRank;
    private List<TopUserByPoint> topUsersByPoint;
    private Long purchasingUsers;
    private Long registeredOnlyUsers;
    private Double purchaseRate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRankStatistic {
        private String rankName;
        private Long userCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUserByPoint {
        private String fullName;
        private String email;
        private Integer totalPoint;
        private String rankName;
    }
}
