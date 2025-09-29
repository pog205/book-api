package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundDecisionDto {
    
    @NotNull(message = "Order ID không được để trống")
    private Long orderId;
    
    @NotNull(message = "Admin ID không được để trống")
    private Long adminId;
    
    @NotNull(message = "Quyết định không được để trống")
    private Boolean approved; // true = chấp nhận, false = từ chối
    
    @NotBlank(message = "Lý do quyết định không được để trống")
    @Size(max = 1000, message = "Lý do quyết định không được vượt quá 1000 ký tự")
    private String adminNotes;
}
