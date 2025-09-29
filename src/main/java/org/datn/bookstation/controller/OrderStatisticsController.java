package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.response.*;
import org.datn.bookstation.service.OrderStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-statistics")
@RequiredArgsConstructor
@Slf4j
public class OrderStatisticsController {
    
    private final OrderStatisticsService statisticsService;
    
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<OrderOverviewResponse>> getOverviewStatistics() {
        log.info("Getting simple overview statistics");
        OrderOverviewResponse response = statisticsService.getOrderOverview();
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê tổng quan đơn giản thành công", response));
    }
    
    @GetMapping("/overview-detailed")
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getDetailedOverviewStatistics() {
        log.info("Getting detailed overview statistics");
        OrderStatisticsResponse response = statisticsService.getOrderStatistics();
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê tổng quan chi tiết thành công", response));
    }
    
    @GetMapping("/revenue-chart")
    public ResponseEntity<ApiResponse<RevenueChartResponse>> getRevenueChart(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) Integer days) {
        log.info("Getting revenue chart for period: {}, days: {}", period, days);
        RevenueChartResponse response = statisticsService.getRevenueChart(period, days);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy biểu đồ doanh thu thành công", response));
    }
    
    @GetMapping("/top-products")
    public ResponseEntity<ApiResponse<TopProductsResponse>> getTopProducts(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(required = false) Integer limit) {
        log.info("Getting top products for period: {}, limit: {}", period, limit);
        TopProductsResponse response = statisticsService.getTopProducts(period, limit);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy top sản phẩm thành công", response));
    }
    
    @GetMapping("/payment-methods")
    public ResponseEntity<ApiResponse<PaymentMethodStatsResponse>> getPaymentMethodStats(
            @RequestParam(defaultValue = "week") String period) {
        log.info("Getting payment method stats for period: {}", period);
        PaymentMethodStatsResponse response = statisticsService.getPaymentMethodStats(period);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê phương thức thanh toán thành công", response));
    }
    
    @GetMapping("/locations")
    public ResponseEntity<ApiResponse<LocationStatsResponse>> getLocationStats(
            @RequestParam(defaultValue = "week") String period) {
        log.info("Getting location stats for period: {}", period);
        LocationStatsResponse response = statisticsService.getLocationStats(period);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê theo địa điểm thành công", response));
    }
    
    @GetMapping("/revenue-comparison")
    public ResponseEntity<ApiResponse<RevenueComparisonResponse>> getRevenueComparison() {
        log.info("Getting revenue comparison");
        RevenueComparisonResponse response = statisticsService.getRevenueComparison();
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy so sánh doanh thu thành công", response));
    }
    
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<CustomerStatsResponse>> getCustomerStats(
            @RequestParam(defaultValue = "month") String period) {
        log.info("Getting customer stats for period: {}", period);
        CustomerStatsResponse response = statisticsService.getCustomerStats(period);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thống kê khách hàng thành công", response));
    }
    
    @GetMapping("/cross-sell/{orderId}")
    public ResponseEntity<ApiResponse<CrossSellSuggestionResponse>> getCrossSellSuggestions(
            @PathVariable Integer orderId,
            @RequestParam(required = false) Integer limit) {
        log.info("Getting cross-sell suggestions for order: {}, limit: {}", orderId, limit);
        CrossSellSuggestionResponse response = statisticsService.getCrossSellSuggestions(orderId, limit);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy gợi ý bán chéo thành công", response));
    }
}
