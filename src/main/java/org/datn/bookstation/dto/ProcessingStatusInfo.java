package org.datn.bookstation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ✅ DTO chứa thông tin trạng thái thực tế và lý do đang xử lý
 * Giúp frontend hiểu rõ tại sao một order detail đang được xử lý
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingStatusInfo {
    
    private String actualStatus;           // Trạng thái thực tế kết hợp order + refund
    private String actualStatusDisplay;    // Mô tả trạng thái thực tế (tiếng Việt)
    private String processingReason;       // Lý do tại sao đang xử lý
    
    // ✅ Static factory methods cho các trường hợp phổ biến
    public static ProcessingStatusInfo forOrderStatus(String orderStatus, String orderStatusDisplay, String reason) {
        return ProcessingStatusInfo.builder()
            .actualStatus(orderStatus)
            .actualStatusDisplay(orderStatusDisplay)
            .processingReason(reason)
            .build();
    }
    
    public static ProcessingStatusInfo forRefundStatus(String refundStatus, String refundStatusDisplay, String reason) {
        return ProcessingStatusInfo.builder()
            .actualStatus(refundStatus)
            .actualStatusDisplay(refundStatusDisplay)
            .processingReason(reason)
            .build();
    }
    
    public static ProcessingStatusInfo forCombinedStatus(String combinedStatus, String combinedStatusDisplay, String reason) {
        return ProcessingStatusInfo.builder()
            .actualStatus(combinedStatus)
            .actualStatusDisplay(combinedStatusDisplay)
            .processingReason(reason)
            .build();
    }
}
