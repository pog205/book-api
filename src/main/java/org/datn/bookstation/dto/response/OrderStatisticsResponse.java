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
public class OrderStatisticsResponse {
    
    // Tổng số đơn hàng theo khoảng thời gian
    private Long totalOrdersToday;
    private Long totalOrdersThisMonth;
    
    // Doanh thu và lợi nhuận
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
    private BigDecimal netProfitToday;
    private BigDecimal netProfitThisMonth;
    
    // ✅ THÊM: Doanh thu trung bình trên mỗi đơn 
    private BigDecimal averageRevenuePerOrderToday;
    private BigDecimal averageRevenuePerOrderThisMonth;
    
    // Chi phí vận chuyển
    private BigDecimal totalShippingCostToday;
    private BigDecimal totalShippingCostThisMonth;
    
    // Tỷ lệ COD
    private Double codRateToday;
    private Double codRateThisMonth;
    
    // Đơn hàng hoàn trả/hủy
    private Long refundedOrdersToday;
    private Long refundedOrdersThisMonth;
    private Long canceledOrdersToday;
    private Long canceledOrdersThisMonth;
    
    // COD thất bại
    private Long failedCodOrdersToday;
    private Long failedCodOrdersThisMonth;
    private Double failedCodRateToday;
    private Double failedCodRateThisMonth;
}
