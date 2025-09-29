package org.datn.bookstation.dto.response.minigame;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignProbabilityResponse {
    private Integer campaignId;
    private String campaignName;
    private BigDecimal totalActiveProbability; // Tổng % của các rewards đang active
    private BigDecimal remainingProbability; // % còn lại có thể dùng cho rewards khác
    private Integer activeRewardsCount; // Số lượng rewards đang active
    private Integer totalRewardsCount; // Tổng số rewards
}
