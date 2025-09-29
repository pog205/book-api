package org.datn.bookstation.dto.response.minigame;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.datn.bookstation.entity.enums.RewardType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponse {
    
    private Integer id;
    private Integer campaignId;
    private RewardType type;
    private String name;
    private String description;
    
    // Thông tin voucher (nếu có)
    private Integer voucherId;
    private String voucherCode;
    private String voucherName;
    private String voucherDescription;
    
    // Giá trị điểm (nếu có)
    private Integer pointValue;
    
    private Integer stock; // Số lượng còn lại
    private BigDecimal probability;
    private Byte status;
    
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    
    // Thông tin thống kê
    private Integer distributedCount;
    private BigDecimal distributedPercentage;
}
