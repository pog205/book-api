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
public class OrderOverviewResponse {
    
    // Tổng số đơn hàng
    private Long totalOrdersToday;
    private Long totalOrdersThisMonth;
    
    // Doanh thu thuần (chỉ tính đơn đã hoàn thành - DELIVERED)
    private BigDecimal netRevenueToday;
    private BigDecimal netRevenueThisMonth;
    
    // Số đơn hoàn trả
    private Long refundedOrdersToday;
    private Long refundedOrdersThisMonth;
    
    // Số đơn hủy
    private Long canceledOrdersToday;
    private Long canceledOrdersThisMonth;
}
