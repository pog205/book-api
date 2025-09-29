package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.QuickActionRequest;
import org.datn.bookstation.dto.response.*;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.OrderRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.service.AdvancedAnalyticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvancedAnalyticsServiceImpl implements AdvancedAnalyticsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    private static final List<OrderStatus> SUCCESS_STATUSES = Arrays.asList(
            OrderStatus.DELIVERED,
            OrderStatus.PARTIALLY_REFUNDED
    );

    private static final List<OrderStatus> FAILED_STATUSES = Arrays.asList(
            OrderStatus.DELIVERY_FAILED,
            OrderStatus.CANCELED,
            OrderStatus.RETURNING_TO_WAREHOUSE
    );

    private static final List<OrderStatus> REFUND_CANCEL_STATUSES = Arrays.asList(
            OrderStatus.REFUNDED,
            OrderStatus.PARTIALLY_REFUNDED,
            OrderStatus.CANCELED
    );

    @Override
    public SurvivalKpiResponse getSurvivalKpis() {
        log.info(" Getting survival KPIs...");
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate lastWeekStart = weekStart.minusDays(7);
            LocalDate lastWeekEnd = weekStart.minusDays(1);

            // Daily Orders Metric
            SurvivalKpiResponse.DailyOrderMetric dailyOrders = calculateDailyOrdersMetric(today, yesterday);
            
            // Weekly Revenue Metric  
            SurvivalKpiResponse.WeeklyRevenueMetric weeklyRevenue = calculateWeeklyRevenueMetric(weekStart, lastWeekStart, lastWeekEnd);
            
            // Refund/Cancel Rate
            SurvivalKpiResponse.RefundCancelMetric refundCancelRate = calculateRefundCancelRate();
            
            // Retention Rate
            SurvivalKpiResponse.RetentionMetric retentionRate = calculateRetentionRate();
            
            // Instant Actions
            List<SurvivalKpiResponse.InstantAction> instantActions = generateInstantActions(weeklyRevenue, refundCancelRate);

            return SurvivalKpiResponse.builder()
                    .dailyOrders(dailyOrders)
                    .weeklyRevenue(weeklyRevenue)
                    .refundCancelRate(refundCancelRate)
                    .retentionRate(retentionRate)
                    .instantActions(instantActions)
                    .lastUpdated(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error(" Error getting survival KPIs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get survival KPIs", e);
        }
    }

    @Override
    public OpportunityRadarResponse getOpportunityRadar() {
        log.info(" Getting opportunity radar...");
        
        try {
            // Hot Today Books (mock data for now - would use real inventory system)
            List<OpportunityRadarResponse.HotTodayBook> hotBooks = generateMockHotBooks();
            
            // Trending Books (mock data - would integrate with social media APIs)
            List<OpportunityRadarResponse.TrendingBook> trendingBooks = generateMockTrendingBooks();
            
            // VIP Returning Soon (basic prediction based on order history)
            List<OpportunityRadarResponse.VipReturning> vipReturning = predictVipReturning();
            
            // Abandoned Carts (mock data - would use cart tracking)
            List<OpportunityRadarResponse.AbandonedCart> abandonedCarts = generateMockAbandonedCarts();

            return OpportunityRadarResponse.builder()
                    .hotTodayBooks(hotBooks)
                    .trendingBooks(trendingBooks)
                    .vipReturningSoon(vipReturning)
                    .abandonedCarts(abandonedCarts)
                    .lastUpdated(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error(" Error getting opportunity radar: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get opportunity radar", e);
        }
    }

    @Override
    public OrderHealthMapResponse getOrderHealthMap() {
        log.info(" Getting order health map...");
        
        try {
            // Region Health Analysis
            List<OrderHealthMapResponse.RegionHealth> regionHealth = analyzeRegionHealth();
            
            // Shipping Partners Performance
            List<OrderHealthMapResponse.ShippingPartner> shippingPartners = analyzeShippingPartners();
            
            // Risk Orders Detection
            List<OrderHealthMapResponse.RiskOrder> riskOrders = detectRiskOrders();
            
            // Calculate Overall Health Score
            double overallHealth = calculateOverallHealthScore(regionHealth);

            return OrderHealthMapResponse.builder()
                    .regionHealth(regionHealth)
                    .shippingPartners(shippingPartners)
                    .riskOrders(riskOrders)
                    .overallHealthScore(overallHealth)
                    .healthStatus(getHealthStatus(overallHealth))
                    .lastUpdated(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error(" Error getting order health map: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get order health map", e);
        }
    }

    @Override
    public RealTimeAlertsResponse getRealTimeAlerts() {
        log.info(" Getting real-time alerts...");
        
        try {
            List<RealTimeAlertsResponse.Alert> criticalAlerts = new ArrayList<>();
            List<RealTimeAlertsResponse.Alert> warningAlerts = new ArrayList<>();
            List<RealTimeAlertsResponse.Alert> infoAlerts = new ArrayList<>();

            // Check for COD unconfirmed orders
            checkCodUnconfirmedOrders(criticalAlerts);
            
            // Check for low inventory
            checkLowInventory(criticalAlerts);
            
            // Check for revenue drops
            checkRevenueDrops(warningAlerts);
            
            // Check for trending opportunities
            checkTrendingOpportunities(infoAlerts);

            RealTimeAlertsResponse.AlertStats stats = RealTimeAlertsResponse.AlertStats.builder()
                    .totalActive(criticalAlerts.size() + warningAlerts.size() + infoAlerts.size())
                    .critical(criticalAlerts.size())
                    .warning(warningAlerts.size())
                    .info(infoAlerts.size())
                    .resolved24h(15) // Mock data
                    .build();

            return RealTimeAlertsResponse.builder()
                    .criticalAlerts(criticalAlerts)
                    .warningAlerts(warningAlerts)
                    .infoAlerts(infoAlerts)
                    .alertStats(stats)
                    .lastUpdated(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error(" Error getting real-time alerts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get real-time alerts", e);
        }
    }

    @Override
    public QuickActionResponse executeQuickAction(QuickActionRequest request) {
        log.info(" Executing quick action: {}", request.getActionType());
        
        try {
            String actionId = "action_" + System.currentTimeMillis();
            
            QuickActionResponse.ActionResult result;
            
            switch (request.getActionType()) {
                case "CREATE_FLASH_SALE":
                    result = executeFlashSaleCreation(request.getParameters());
                    break;
                case "SEND_VIP_VOUCHER":
                    result = executeVipVoucherSending(request.getParameters());
                    break;
                case "BULK_INVENTORY_ORDER":
                    result = executeBulkInventoryOrder(request.getParameters());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action type: " + request.getActionType());
            }

            return QuickActionResponse.builder()
                    .actionId(actionId)
                    .actionType(request.getActionType())
                    .status("completed")
                    .result(result)
                    .executedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error(" Error executing quick action: {}", e.getMessage(), e);
            return QuickActionResponse.builder()
                    .actionId("action_" + System.currentTimeMillis())
                    .actionType(request.getActionType())
                    .status("failed")
                    .errorMessage(e.getMessage())
                    .executedAt(LocalDateTime.now())
                    .build();
        }
    }

    // Helper Methods

    private SurvivalKpiResponse.DailyOrderMetric calculateDailyOrdersMetric(LocalDate today, LocalDate yesterday) {
        // Mock implementation - would use real order counts
        Integer todayOrders = 45;
        Integer yesterdayOrders = 52;
        
        double growthRate = yesterdayOrders > 0 ? 
            ((double)(todayOrders - yesterdayOrders) / yesterdayOrders) * 100 : 0;
        
        String trend = growthRate > 0 ? "up" : growthRate < 0 ? "down" : "stable";
        String alertLevel = growthRate < -20 ? "critical" : growthRate < -10 ? "warning" : "green";

        return SurvivalKpiResponse.DailyOrderMetric.builder()
                .today(todayOrders)
                .yesterday(yesterdayOrders)
                .growthRate(Math.round(growthRate * 100.0) / 100.0)
                .trend(trend)
                .alertLevel(alertLevel)
                .build();
    }

    private SurvivalKpiResponse.WeeklyRevenueMetric calculateWeeklyRevenueMetric(LocalDate weekStart, LocalDate lastWeekStart, LocalDate lastWeekEnd) {
        // Mock implementation
        BigDecimal thisWeek = new BigDecimal("125000000.00");
        BigDecimal lastWeek = new BigDecimal("156000000.00");
        
        double growthRate = lastWeek.compareTo(BigDecimal.ZERO) > 0 ? 
            thisWeek.subtract(lastWeek).divide(lastWeek, 4, RoundingMode.HALF_UP).doubleValue() * 100 : 0;
        
        String trend = growthRate > 0 ? "up" : "down";
        String alertLevel = growthRate < -20 ? "critical" : growthRate < -10 ? "warning" : "green";
        String actionSuggestion = growthRate < -15 ? "Doanh thu giảm 20% so với tuần trước → tăng khuyến mãi Flash Sale cho 5 đầu sách hot" : null;

        return SurvivalKpiResponse.WeeklyRevenueMetric.builder()
                .thisWeek(thisWeek)
                .lastWeek(lastWeek)
                .growthRate(Math.round(growthRate * 100.0) / 100.0)
                .trend(trend)
                .alertLevel(alertLevel)
                .actionSuggestion(actionSuggestion)
                .build();
    }

    private SurvivalKpiResponse.RefundCancelMetric calculateRefundCancelRate() {
        // Mock calculation
        double rate = 7.2;
        double threshold = 5.0;
        String alertLevel = rate > threshold ? "critical" : "green";
        String actionSuggestion = rate > threshold ? "Tỷ lệ hoàn hủy 7.2% > 5% → kiểm tra chất lượng giao hàng" : null;

        return SurvivalKpiResponse.RefundCancelMetric.builder()
                .rate(rate)
                .threshold(threshold)
                .alertLevel(alertLevel)
                .totalRefundCancel(36)
                .totalOrders(500)
                .actionSuggestion(actionSuggestion)
                .build();
    }

    private SurvivalKpiResponse.RetentionMetric calculateRetentionRate() {
        // Mock calculation
        double rate = 68.5;
        double previousRate = 72.1;
        String trend = rate < previousRate ? "down" : "up";
        String alertLevel = rate < 70 ? "warning" : "green";

        return SurvivalKpiResponse.RetentionMetric.builder()
                .rate(rate)
                .previousRate(previousRate)
                .trend(trend)
                .returningCustomers(342)
                .totalCustomers(499)
                .alertLevel(alertLevel)
                .build();
    }

    private List<SurvivalKpiResponse.InstantAction> generateInstantActions(
            SurvivalKpiResponse.WeeklyRevenueMetric weeklyRevenue, 
            SurvivalKpiResponse.RefundCancelMetric refundRate) {
        
        List<SurvivalKpiResponse.InstantAction> actions = new ArrayList<>();

        if ("critical".equals(weeklyRevenue.getAlertLevel())) {
            actions.add(SurvivalKpiResponse.InstantAction.builder()
                    .priority("high")
                    .type("flash_sale")
                    .message("Tạo Flash Sale 30% cho Top 5 sách hot")
                    .actionUrl("/admin/flash-sales/create")
                    .estimatedImpact("Tăng 25% doanh thu")
                    .build());
        }

        if ("critical".equals(refundRate.getAlertLevel())) {
            actions.add(SurvivalKpiResponse.InstantAction.builder()
                    .priority("medium")
                    .type("quality_check")
                    .message("Kiểm tra chất lượng đóng gói và giao hàng")
                    .actionUrl("/admin/quality/review")
                    .estimatedImpact("Giảm 30% tỷ lệ hoàn trả")
                    .build());
        }

        return actions;
    }

    // Mock data generators (would be replaced with real implementations)

    private List<OpportunityRadarResponse.HotTodayBook> generateMockHotBooks() {
        return Arrays.asList(
            OpportunityRadarResponse.HotTodayBook.builder()
                .bookId(123L)
                .bookTitle("Atomic Habits")
                .coverImageUrl("/images/atomic-habits.jpg")
                .soldToday(25)
                .currentStock(12)
                .stockoutRisk("high")
                .estimatedStockoutDate(LocalDate.now().plusDays(2))
                .reorderSuggestion(50)
                .actionUrl("/admin/inventory/reorder/123")
                .build()
        );
    }

    private List<OpportunityRadarResponse.TrendingBook> generateMockTrendingBooks() {
        return Arrays.asList(
            OpportunityRadarResponse.TrendingBook.builder()
                .bookId(456L)
                .bookTitle("Think and Grow Rich")
                .coverImageUrl("/images/think-grow-rich.jpg")
                .socialMentions(1250)
                .trendScore(0.89)
                .currentStock(45)
                .suggestedOrder(100)
                .trendSource("TikTok, Facebook")
                .actionUrl("/admin/inventory/bulk-order/456")
                .build()
        );
    }

    private List<OpportunityRadarResponse.VipReturning> predictVipReturning() {
        // Mock VIP prediction - would use ML model in production
        return Arrays.asList(
            OpportunityRadarResponse.VipReturning.builder()
                .userId(789L)
                .customerName("Nguyễn Văn A")
                .email("nguyenvana@gmail.com")
                .predictedReturnDate(LocalDate.now().plusDays(3))
                .averageOrderValue(new BigDecimal("850000.00"))
                .preferredCategories(Arrays.asList("Self-help", "Business"))
                .confidence(0.92)
                .actionUrl("/admin/customers/send-voucher/789")
                .build()
        );
    }

    private List<OpportunityRadarResponse.AbandonedCart> generateMockAbandonedCarts() {
        return Arrays.asList(
            OpportunityRadarResponse.AbandonedCart.builder()
                .userId(101L)
                .customerName("Trần Thị B")
                .email("tranthib@gmail.com")
                .cartValue(new BigDecimal("450000.00"))
                .cartAge("2 hours")
                .riskScore(0.78)
                .suggestedDiscount(10)
                .actionUrl("/admin/marketing/recovery-email/101")
                .build()
        );
    }

    // Region and health analysis methods would go here...
    private List<OrderHealthMapResponse.RegionHealth> analyzeRegionHealth() {
        // Mock implementation
        return Arrays.asList(
            OrderHealthMapResponse.RegionHealth.builder()
                .provinceName("Hồ Chí Minh")
                .provinceId(79)
                .healthScore(0.95)
                .orderGrowth(15.2)
                .deliverySuccessRate(94.5)
                .status("excellent")
                .alertLevel("green")
                .build(),
            OrderHealthMapResponse.RegionHealth.builder()
                .provinceName("Hà Nội")
                .provinceId(1)
                .healthScore(0.65)
                .orderGrowth(-30.1)
                .deliverySuccessRate(87.2)
                .status("concerning")
                .alertLevel("red")
                .actionSuggestion("Khu vực Hà Nội giảm 30% → tăng marketing địa phương")
                .build()
        );
    }

    private List<OrderHealthMapResponse.ShippingPartner> analyzeShippingPartners() {
        return Arrays.asList(
            OrderHealthMapResponse.ShippingPartner.builder()
                .partnerName("Giao Hàng Nhanh")
                .successRate(96.2)
                .avgDeliveryTime(2.5)
                .failureReasons(Arrays.asList("Khách không nghe máy", "Sai địa chỉ"))
                .alertLevel("green")
                .totalOrders(1250)
                .successfulOrders(1203)
                .build()
        );
    }

    private List<OrderHealthMapResponse.RiskOrder> detectRiskOrders() {
        return Arrays.asList(
            OrderHealthMapResponse.RiskOrder.builder()
                .orderId(12345L)
                .customerName("Lê Văn C")
                .customerPhone("0987654321")
                .riskType("COD_BOMB")
                .riskScore(0.85)
                .daysOverdue(2)
                .orderValue(new BigDecimal("650000.00"))
                .actionUrl("/admin/orders/contact/12345")
                .riskReason("Khách không nhận máy sau 3 lần gọi")
                .build()
        );
    }

    private double calculateOverallHealthScore(List<OrderHealthMapResponse.RegionHealth> regions) {
        return regions.stream()
                .mapToDouble(OrderHealthMapResponse.RegionHealth::getHealthScore)
                .average()
                .orElse(0.75);
    }

    private String getHealthStatus(double score) {
        if (score >= 0.9) return "Excellent";
        if (score >= 0.75) return "Good";
        if (score >= 0.6) return "Concerning";
        return "Critical";
    }

    // Alert checking methods
    private void checkCodUnconfirmedOrders(List<RealTimeAlertsResponse.Alert> alerts) {
        // Mock alert - would check real unconfirmed COD orders
        alerts.add(RealTimeAlertsResponse.Alert.builder()
                .id("alert_001")
                .type("COD_UNCONFIRMED")
                .severity("high")
                .title("15 đơn COD chưa xác nhận sau 24h")
                .message("⚠️ 15 đơn COD chưa xác nhận sau 24h → dễ bị bom")
                .affectedCount(15)
                .totalValue(new BigDecimal("12500000.00"))
                .actionUrl("/admin/orders/cod-confirmation")
                .createdAt(LocalDateTime.now())
                .priority(1)
                .build());
    }

    private void checkLowInventory(List<RealTimeAlertsResponse.Alert> alerts) {
        alerts.add(RealTimeAlertsResponse.Alert.builder()
                .id("alert_002")
                .type("LOW_INVENTORY")
                .severity("medium")
                .title("Sách hot sắp hết hàng")
                .message("🔥 Ấn phẩm Harry Potter bản giới hạn chỉ còn 8 cuốn")
                .bookId(999L)
                .currentStock(8)
                .dailyAvgSales(3)
                .stockoutEstimate("3 days")
                .actionUrl("/admin/inventory/reorder/999")
                .priority(2)
                .build());
    }

    private void checkRevenueDrops(List<RealTimeAlertsResponse.Alert> alerts) {
        alerts.add(RealTimeAlertsResponse.Alert.builder()
                .id("alert_003")
                .type("REVENUE_DROP")
                .severity("medium")
                .title("Doanh thu khu vực giảm mạnh")
                .message("📉 Khu vực Hà Nội giảm 30% doanh thu so với tuần trước")
                .region("Hà Nội")
                .dropPercentage(-30.1)
                .actionUrl("/admin/marketing/regional-campaign")
                .priority(3)
                .build());
    }

    private void checkTrendingOpportunities(List<RealTimeAlertsResponse.Alert> alerts) {
        alerts.add(RealTimeAlertsResponse.Alert.builder()
                .id("alert_004")
                .type("TREND_OPPORTUNITY")
                .severity("low")
                .title("Cơ hội trending mới")
                .message("📈 Sách 'Tâm lý học đám đông' đang viral TikTok")
                .socialMentions(2500)
                .actionUrl("/admin/inventory/trend-analysis")
                .priority(4)
                .build());
    }

    // Action execution methods
    private QuickActionResponse.ActionResult executeFlashSaleCreation(Map<String, Object> parameters) {
        // Mock flash sale creation
        return QuickActionResponse.ActionResult.builder()
                .flashSaleId("789")
                .flashSaleName("Flash Sale Auto - Top Books 30%")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(25))
                .expectedImpact("Tăng 25% doanh thu trong 24h")
                .trackingUrl("/admin/flash-sales/789")
                .build();
    }

    private QuickActionResponse.ActionResult executeVipVoucherSending(Map<String, Object> parameters) {
        // Mock voucher sending
        return QuickActionResponse.ActionResult.builder()
                .vouchersSent(150)
                .expectedImpact("Tăng 15% retention rate")
                .trackingUrl("/admin/vouchers/campaign/abc123")
                .build();
    }

    private QuickActionResponse.ActionResult executeBulkInventoryOrder(Map<String, Object> parameters) {
        // Mock bulk order
        return QuickActionResponse.ActionResult.builder()
                .ordersCreated(5)
                .expectedImpact("Đảm bảo stock cho 2 tuần tới")
                .trackingUrl("/admin/inventory/bulk-orders/xyz456")
                .build();
    }
}
