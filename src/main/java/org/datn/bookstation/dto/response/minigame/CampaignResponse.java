package org.datn.bookstation.dto.response.minigame;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {
    
    private Integer id;
    private String name;
    private Long startDate;
    private Long endDate;
    private Byte status;
    private Integer configFreeLimit;
    private Integer configPointCost;
    private String description;
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    
    // Thông tin thống kê bổ sung
    private Integer totalParticipants;
    private Long totalOpened;
    private Integer totalRewards;
    private Integer remainingRewards;
    private List<RewardResponse> rewards;
    
    // Thông tin cho user hiện tại (nếu có)
    private Integer userFreeOpenedCount;
    private Integer userTotalOpenedCount;
    private Integer userRemainingFreeOpens;
}
