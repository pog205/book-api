package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodStatsResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodData {
        private String paymentMethod; // "COD", "ONLINE"
        private Long orderCount;
        private BigDecimal totalAmount;
        private Double percentage;
    }
    
    private List<PaymentMethodData> paymentMethods;
    private Long totalOrders;
    private BigDecimal totalAmount;
}
