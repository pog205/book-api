package org.datn.bookstation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestDto {
    
    @NotNull(message = "User ID không được để trống")
    private Long userId;
    
    @NotBlank(message = "Lý do hoàn trả không được để trống")
    @Size(max = 500, message = "Lý do hoàn trả không được vượt quá 500 ký tự")
    private String reason;
    
    @Size(max = 1000, message = "Ghi chú thêm không được vượt quá 1000 ký tự")
    private String additionalNotes;
    
    @NotEmpty(message = "Chi tiết hoàn trả không được để trống")
    @Valid
    private List<OrderDetailRefundRequest> refundDetails;
    
    // Evidence files paths (đã upload trước đó)
    private List<String> evidenceImages;
    private List<String> evidenceVideos;
}
