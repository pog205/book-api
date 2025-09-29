package org.datn.bookstation.dto.request.minigame;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.datn.bookstation.entity.enums.BoxOpenType;

@Data
public class OpenBoxRequest {
    
    @NotNull(message = "Campaign ID không được để trống")
    private Integer campaignId;
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    @NotNull(message = "Loại mở hộp không được để trống")
    private BoxOpenType openType; // FREE hoặc POINT
    
    // ===== VALIDATION FIELDS - Dữ liệu frontend gửi lên để validate với backend =====
    
    @NotNull(message = "Số lượt free limit từ frontend không được để trống")
    private Integer frontendFreeLimit; // Số lượt free mà frontend đang hiển thị
    
    @NotNull(message = "Điểm cost từ frontend không được để trống")  
    private Integer frontendPointCost; // Số điểm để mở hộp mà frontend đang hiển thị
    
    @NotNull(message = "Start date từ frontend không được để trống")
    private Long frontendStartDate; // Ngày bắt đầu chiến dịch mà frontend đang hiển thị
    
    @NotNull(message = "End date từ frontend không được để trống")
    private Long frontendEndDate; // Ngày kết thúc chiến dịch mà frontend đang hiển thị
    
    @NotNull(message = "User point từ frontend không được để trống")
    private Integer frontendUserPoint; // Số điểm user mà frontend đang hiển thị
    
    @NotNull(message = "Free opened count từ frontend không được để trống")
    private Integer frontendFreeOpenedCount; // Số lần đã mở free mà frontend đang hiển thị
}
