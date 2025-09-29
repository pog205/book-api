package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingOrderResponse {
    
    // ✅ THÔNG TIN CƠ BẢN CỦA ĐƠN HÀNG
    private Integer orderId;
    private String orderCode;
    
    // ✅ TRẠNG THÁI VÀ SỐ LƯỢNG ĐANG XỬ LÝ  
    private Integer processingQuantity; // Số lượng THỰC SỰ đang được xử lý
    private String statusDisplay; // Trạng thái hiển thị rõ ràng (tiếng Việt)
}
