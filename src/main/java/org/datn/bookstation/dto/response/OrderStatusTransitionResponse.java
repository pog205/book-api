package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.datn.bookstation.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response cho việc thay đổi trạng thái đơn hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusTransitionResponse {
    
    private Integer orderId;
    private String orderCode;
    private OrderStatus previousStatus;
    private OrderStatus newStatus;
    private String transitionMessage;
    private Long transitionTime;
    
    // Thông tin về các tác động nghiệp vụ
    private BusinessImpactSummary businessImpact;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessImpactSummary {
        private PointImpact pointImpact;
        private StockImpact stockImpact;
        private VoucherImpact voucherImpact;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PointImpact {
            private Integer pointsEarned;
            private Integer pointsDeducted;
            private String description;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class StockImpact {
            private List<StockAdjustment> adjustments;
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class StockAdjustment {
                private Integer bookId;
                private String bookTitle;
                private Integer quantityAdjusted;
                private String adjustmentType; // "RESERVED", "RELEASED", "DEDUCTED"
            }
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VoucherImpact {
            private Integer vouchersUsed;
            private Integer vouchersRefunded;
            private BigDecimal totalDiscountImpacted;
            private String description;
        }
    }
}
