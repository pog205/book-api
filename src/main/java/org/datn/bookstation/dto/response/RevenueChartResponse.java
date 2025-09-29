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
public class RevenueChartResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueDataPoint {
        private String date; // Format: "2025-01-01" 
        private String period; // "Thứ 2", "Tuần 1", "Tháng 1"
        private BigDecimal revenue;
        private Long orderCount;
        
        // 🔥 NEW: Thông tin chi tiết về khoảng thời gian cho weekly/monthly
        private String startDate; // "2025-07-01" - Ngày bắt đầu tuần/tháng
        private String endDate;   // "2025-07-07" - Ngày kết thúc tuần/tháng
        private String dateRange; // "01/07 - 07/07" - Hiển thị khoảng ngày đẹp
    }
    
    private List<RevenueDataPoint> dataPoints;
    private String periodType; // "daily", "weekly", "monthly"
    private BigDecimal totalRevenue;
    private Long totalOrders;
}
