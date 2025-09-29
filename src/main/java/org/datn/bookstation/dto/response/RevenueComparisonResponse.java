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
public class RevenueComparisonResponse {
    
    // So sánh tuần này vs tuần trước
    private BigDecimal currentWeekRevenue;
    private BigDecimal previousWeekRevenue;
    private Double weeklyGrowthRate;
    private String weeklyGrowthDirection; // "up", "down", "same"
    
    // So sánh tháng này vs tháng trước  
    private BigDecimal currentMonthRevenue;
    private BigDecimal previousMonthRevenue;
    private Double monthlyGrowthRate;
    private String monthlyGrowthDirection; // "up", "down", "same"
    
    // Số đơn hàng
    private Long currentWeekOrders;
    private Long previousWeekOrders;
    private Long currentMonthOrders;
    private Long previousMonthOrders;
}
