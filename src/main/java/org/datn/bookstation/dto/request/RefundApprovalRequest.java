package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefundApprovalRequest {
    
    @NotBlank(message = "Trạng thái phê duyệt không được để trống")
    private String status; // "APPROVED" hoặc "REJECTED"
    
    private String adminNote; // Ghi chú từ admin
    
    // ✅ THÊM MỚI: Thông tin từ chối chi tiết
    private String rejectReason; // Lý do từ chối: "DAMAGED_BY_USER", "INVALID_CLAIM", "MISSING_EVIDENCE", etc.
    private String rejectReasonDisplay; // Hiển thị lý do từ chối
    private String suggestedAction; // Gợi ý hành động cho khách hàng
}
