package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RefundRequestCreate {
    
    @NotNull(message = "ID đơn hàng không được để trống")
    private Integer orderId;
    
    @NotNull(message = "Loại hoàn trả không được để trống")
    private String refundType; // "PARTIAL" hoặc "FULL"
    
    @NotBlank(message = "Lý do hoàn trả không được để trống")
    private String reason;
    
    private String customerNote;
    
    // ✅ Evidence files
    private List<String> evidenceImages; // URLs ảnh bằng chứng
    private List<String> evidenceVideos; // URLs video bằng chứng
    
    // ✅ Cho hoàn trả một phần - danh sách sản phẩm
    private List<RefundItemRequest> refundItems;
    
    @Data
    public static class RefundItemRequest {
        @NotNull(message = "ID sách không được để trống")
        private Integer bookId;
        
        @NotNull(message = "Số lượng hoàn trả không được để trống")
        private Integer refundQuantity;
        
        private String reason; // Lý do hoàn trả sản phẩm cụ thể này
    }
}
