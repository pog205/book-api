package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.datn.bookstation.entity.enums.OrderStatus;

import java.util.List;

/**
 * Response DTO cho API lấy danh sách trạng thái có thể chuyển
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableStatusTransitionResponse {
    
    /**
     * ID đơn hàng
     */
    private Integer orderId;
    
    /**
     * Mã đơn hàng
     */
    private String orderCode;
    
    /**
     * Trạng thái hiện tại của đơn hàng
     */
    private OrderStatus currentStatus;
    
    /**
     * Tên hiển thị của trạng thái hiện tại
     */
    private String currentStatusDisplayName;
    
    /**
     * Danh sách trạng thái có thể chuyển
     */
    private List<StatusTransitionOption> availableTransitions;
    
    /**
     * Kiểm tra xem đơn hàng có đã thanh toán online hay không
     */
    private Boolean isPaidOnline;
    
    /**
     * Lý do tại sao một số trạng thái không thể chuyển (nếu có)
     */
    private String restrictionNote;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusTransitionOption {
        /**
         * Trạng thái đích
         */
        private OrderStatus targetStatus;
        
        /**
         * Tên hiển thị của trạng thái đích
         */
        private String displayName;
        
        /**
         * Mô tả hành động chuyển đổi
         */
        private String actionDescription;
        
        /**
         * Có yêu cầu xác nhận đặc biệt không
         */
        private Boolean requiresConfirmation;
        
        /**
         * Ghi chú về tác động nghiệp vụ
         */
        private String businessImpactNote;
    }
}
