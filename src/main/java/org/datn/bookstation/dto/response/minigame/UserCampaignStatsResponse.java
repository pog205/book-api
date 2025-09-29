package org.datn.bookstation.dto.response.minigame;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCampaignStatsResponse {
    
    private Integer userId;
    private String userName;
    private Integer campaignId;
    private String campaignName;
    
    private Integer freeOpenedCount;
    private Integer totalOpenedCount;
    private Integer remainingFreeOpens;
    
    // Thống kê thắng thua
    private Long totalWins;
    private Long totalLoses;
    private Double winRate;
    
    // Thống kê phần thưởng
    private Integer totalVouchersWon;
    private Integer totalPointsWon;
    private Integer totalPointsSpent;
    
    private Long createdAt;
    private Long updatedAt;
}
