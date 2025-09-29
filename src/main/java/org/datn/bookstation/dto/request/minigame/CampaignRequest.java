package org.datn.bookstation.dto.request.minigame;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CampaignRequest {
    
    private Integer id;
    
    @NotBlank(message = "Tên chiến dịch không được để trống")
    private String name;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private Long startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống") 
    private Long endDate;
    
    @NotNull(message = "Số lượt free không được để trống")
    @Min(value = 0, message = "Số lượt free phải >= 0")
    private Integer configFreeLimit;
    
    @NotNull(message = "Chi phí điểm không được để trống")
    @Min(value = 1, message = "Chi phí điểm phải >= 1")
    private Integer configPointCost;
    
    private String description;
    
    private Byte status = 1;
    
    private Integer createdBy;
    
    private Integer updatedBy;
}
