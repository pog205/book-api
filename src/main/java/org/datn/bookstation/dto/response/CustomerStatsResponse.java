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
public class CustomerStatsResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerData {
        private Integer userId;
        private String customerName;
        private String email;
        private String phone;
        private Long totalOrders;
        private BigDecimal totalSpent;
        private String customerType; // "new", "returning", "vip", "risky"
        private Double riskScore; // 0.0 to 1.0 (1.0 = highest risk)
        private Long failedDeliveryCount;
        private Long refundRequestCount;
        private String lastOrderDate;
    }
    
    // Khách hàng mới vs quay lại
    private Long newCustomers;
    private Long returningCustomers;
    private Double retentionRate;
    
    // Top khách hàng VIP
    private List<CustomerData> vipCustomers;
    
    // Khách hàng rủi ro cao
    private List<CustomerData> riskyCustomers;
    
    // Tổng thống kê
    private Long totalCustomers;
    private Long activeCustomers; // Có đơn hàng trong tháng
}
