package org.datn.bookstation.dto.response.minigame;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.datn.bookstation.entity.enums.BoxOpenType;
import org.datn.bookstation.entity.enums.RewardType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoxHistoryResponse {    private Integer id;
    private Integer userId;
    private String userName;
    private Integer campaignId;
    private String campaignName;
    private BoxOpenType openType;
    private Long openDate;
    
    // Thông tin phần thưởng
    private Integer rewardId;
    private RewardType rewardType;
    private String rewardName;
    private Integer rewardValue;
    private Integer pointsSpent;
    
    // Thông tin voucher (nếu có)
    private Integer voucherId;
    private String voucherCode;
    private String voucherName;
    
    // Thông tin hiển thị
    private boolean win;
    private String displayResult;
    
    private Long createdAt;
}
