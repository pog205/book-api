package org.datn.bookstation.dto.request.minigame;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import org.datn.bookstation.entity.enums.RewardType;

import java.math.BigDecimal;

@Data
public class RewardRequest {
    
    private Integer id;
    
    @NotNull(message = "Campaign ID không được để trống")
    private Integer campaignId;
    
    @NotNull(message = "Loại phần thưởng không được để trống")
    private RewardType type;
    
    @NotBlank(message = "Tên phần thưởng không được để trống")
    private String name;
    
    private String description;
    
    // Chỉ sử dụng khi type = VOUCHER
    private Integer voucherId;
    
    // Chỉ sử dụng khi type = POINTS  
    @Min(value = 1, message = "Giá trị điểm phải >= 1")
    private Integer pointValue;
    
    @NotNull(message = "Số lượng stock không được để trống")
    @Min(value = 1, message = "Số lượng stock phải >= 1")
    private Integer stock;
    
    @NotNull(message = "Tỷ lệ trúng không được để trống")
    @DecimalMin(value = "0.01", message = "Tỷ lệ trúng phải >= 0.01%")
    @DecimalMax(value = "100.00", message = "Tỷ lệ trúng phải <= 100%")
    private BigDecimal probability;
    
    private Byte status = 1;
    
    private Integer createdBy;
    
    private Integer updatedBy;
}
