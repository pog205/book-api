package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailRefundRequest {
    
    @NotNull(message = "Book ID không được để trống")
    private Integer bookId;
    
    @NotNull(message = "Số lượng hoàn trả không được để trống")
    @Positive(message = "Số lượng hoàn trả phải lớn hơn 0")
    private Integer refundQuantity; // Số lượng cần hoàn trả
    
    @NotNull(message = "Lý do hoàn trả không được để trống")
    private String reason; // Lý do hoàn trả cho sản phẩm này
    
    // ✅ THÊM MỚI: Hỗ trợ ảnh và video minh chứng
    private List<String> evidenceImages; // Danh sách đường dẫn ảnh minh chứng
    private List<String> evidenceVideos; // Danh sách đường dẫn video minh chứng
    
    // ✅ THÊM MỚI: Ghi chú bổ sung
    private String additionalNotes; // Ghi chú bổ sung từ khách hàng
}
