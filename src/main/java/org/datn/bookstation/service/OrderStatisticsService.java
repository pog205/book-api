package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.*;

public interface OrderStatisticsService {
    
    /**
     * API tổng quan đơn giản chỉ gồm: số đơn hôm nay/tháng, doanh thu thuần, số đơn hoàn trả/hủy
     */
    OrderOverviewResponse getOrderOverview();
    
    /**
     * API cho các card thống kê tổng quan dashboard (giữ lại cho backward compatibility)
     * Bao gồm: Tổng số đơn, doanh thu, lợi nhuận, chi phí vận chuyển, tỷ lệ COD, đơn hoàn trả/hủy
     */
    OrderStatisticsResponse getOrderStatistics();
    
    /**
     * API cho biểu đồ đường (line chart) doanh thu theo thời gian
     * @param period "daily", "weekly", "monthly"
     * @param days số ngày lấy dữ liệu (mặc định 30)
     */
    RevenueChartResponse getRevenueChart(String period, Integer days);
    
    /**
     * API cho biểu đồ cột (bar chart) top sản phẩm bán chạy
     * @param period "today", "week", "month"  
     * @param limit số lượng sản phẩm top (mặc định 10)
     */
    TopProductsResponse getTopProducts(String period, Integer limit);
    
    /**
     * API cho biểu đồ tròn (pie chart) tỷ lệ thanh toán COD/Online
     * @param period "today", "week", "month"
     */
    PaymentMethodStatsResponse getPaymentMethodStats(String period);
    
    /**
     * API cho biểu đồ nhiệt (heatmap) theo tỉnh/thành phố
     * @param period "today", "week", "month"
     */
    LocationStatsResponse getLocationStats(String period);
    
    /**
     * API so sánh doanh thu tuần này vs tuần trước, tháng này vs tháng trước
     */
    RevenueComparisonResponse getRevenueComparison();
    
    /**
     * API thống kê khách hàng: mới vs quay lại, retention rate, VIP, rủi ro cao
     * @param period "today", "week", "month"
     */
    CustomerStatsResponse getCustomerStats(String period);
    
    /**
     * API gợi ý bán chéo/bán tăng cho đơn hàng hiện tại
     * @param orderId ID đơn hàng hiện tại
     * @param limit số sản phẩm gợi ý (mặc định 5)
     */
    CrossSellSuggestionResponse getCrossSellSuggestions(Integer orderId, Integer limit);
    
    /**
     * Helper method: Tính net revenue chính xác cho khoảng thời gian
     * Để đảm bảo consistency giữa các API statistics
     * @param startTime thời gian bắt đầu (milliseconds)
     * @param endTime thời gian kết thúc (milliseconds) 
     * @return net revenue sau trừ refunds
     */
    java.math.BigDecimal calculateNetRevenueForPeriod(Long startTime, Long endTime);
}
