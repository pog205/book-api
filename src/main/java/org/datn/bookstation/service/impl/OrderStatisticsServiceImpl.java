package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.response.*;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.OrderRepository;
import org.datn.bookstation.service.OrderStatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatisticsServiceImpl implements OrderStatisticsService {
    
    private final OrderRepository orderRepository;
    
    //  Các trạng thái đơn hàng được tính doanh thu HOÀN TOÀN
    // Chỉ DELIVERED và PARTIALLY_REFUNDED (đã hoàn thành công một phần)
    private static final List<OrderStatus> SUCCESS_STATUSES = Arrays.asList(
        OrderStatus.DELIVERED, 
        OrderStatus.PARTIALLY_REFUNDED
    );
    
    // Các trạng thái đơn hàng COD thất bại
    private static final List<OrderStatus> FAILED_COD_STATUSES = Arrays.asList(
        OrderStatus.DELIVERY_FAILED,
        OrderStatus.CANCELED,
        OrderStatus.RETURNING_TO_WAREHOUSE
    );

    @Override
    public OrderOverviewResponse getOrderOverview() {
        log.info("Getting simple order overview statistics");
        
        Long todayStart = getStartOfDay(0);
        Long todayEnd = getEndOfDay(0);
        Long monthStart = getStartOfMonth(0);
        Long monthEnd = getEndOfMonth(0);
        
        // Tổng số đơn hàng (TẤT CẢ trạng thái)
        Long totalOrdersToday = orderRepository.countAllOrdersByDateRange(todayStart, todayEnd);
        Long totalOrdersThisMonth = orderRepository.countAllOrdersByDateRange(monthStart, monthEnd);
        
        // Doanh thu thuần từ đơn đã DELIVERED trở lên (đã thu tiền thực sự)
        // Chỉ trừ đi khi REFUNDED hoàn tất (chứ không phải REFUND_REQUESTED)
        BigDecimal netRevenueToday = calculateTrueNetRevenue(todayStart, todayEnd);
        BigDecimal netRevenueThisMonth = calculateTrueNetRevenue(monthStart, monthEnd);
        
        // Số đơn hoàn trả (đã hoàn thành việc hoàn trả)
        Long refundedOrdersToday = orderRepository.countByDateRangeAndStatuses(todayStart, todayEnd, 
            Arrays.asList(OrderStatus.REFUNDED, OrderStatus.PARTIALLY_REFUNDED));
        Long refundedOrdersThisMonth = orderRepository.countByDateRangeAndStatuses(monthStart, monthEnd,
            Arrays.asList(OrderStatus.REFUNDED, OrderStatus.PARTIALLY_REFUNDED));
        
        // Số đơn hủy
        Long canceledOrdersToday = orderRepository.countByDateRangeAndStatuses(todayStart, todayEnd, 
            Arrays.asList(OrderStatus.CANCELED));
        Long canceledOrdersThisMonth = orderRepository.countByDateRangeAndStatuses(monthStart, monthEnd,
            Arrays.asList(OrderStatus.CANCELED));
        
        return OrderOverviewResponse.builder()
            .totalOrdersToday(totalOrdersToday)
            .totalOrdersThisMonth(totalOrdersThisMonth)
            .netRevenueToday(netRevenueToday)
            .netRevenueThisMonth(netRevenueThisMonth)
            .refundedOrdersToday(refundedOrdersToday)
            .refundedOrdersThisMonth(refundedOrdersThisMonth)
            .canceledOrdersToday(canceledOrdersToday)
            .canceledOrdersThisMonth(canceledOrdersThisMonth)
            .build();
    }

    @Override
    public OrderStatisticsResponse getOrderStatistics() {
        log.info("Getting order statistics");
        
        Long todayStart = getStartOfDay(0);
        Long todayEnd = getEndOfDay(0);
        Long monthStart = getStartOfMonth(0);
        Long monthEnd = getEndOfMonth(0);
        
        Long totalOrdersToday = orderRepository.countAllOrdersByDateRange(todayStart, todayEnd);
        Long totalOrdersThisMonth = orderRepository.countAllOrdersByDateRange(monthStart, monthEnd);
        
        BigDecimal revenueToday = calculateNetRevenue(todayStart, todayEnd);
        BigDecimal revenueThisMonth = calculateNetRevenue(monthStart, monthEnd);
        
        return OrderStatisticsResponse.builder()
            // Tổng số đơn hàng (TẤT CẢ trạng thái - đây là số đơn được đặt)
            .totalOrdersToday(totalOrdersToday)
            .totalOrdersThisMonth(totalOrdersThisMonth)
            
            //  SỬA: Doanh thu (subtotal trừ đi số tiền đã hoàn trả)
            .revenueToday(revenueToday)
            .revenueThisMonth(revenueThisMonth)
            
            //  THÊM: Doanh thu trung bình trên mỗi đơn
            .averageRevenuePerOrderToday(calculateAverageRevenuePerOrder(revenueToday, totalOrdersToday))
            .averageRevenuePerOrderThisMonth(calculateAverageRevenuePerOrder(revenueThisMonth, totalOrdersThisMonth))
            
            // Lợi nhuận ròng (doanh thu - chi phí vận chuyển, tạm tính đơn giản)
            .netProfitToday(calculateNetProfit(todayStart, todayEnd))
            .netProfitThisMonth(calculateNetProfit(monthStart, monthEnd))
            
            // Chi phí vận chuyển
            .totalShippingCostToday(orderRepository.sumShippingFeeByDateRangeAndStatuses(todayStart, todayEnd, SUCCESS_STATUSES))
            .totalShippingCostThisMonth(orderRepository.sumShippingFeeByDateRangeAndStatuses(monthStart, monthEnd, SUCCESS_STATUSES))
            
            // Tỷ lệ COD
            .codRateToday(calculateCodRate(todayStart, todayEnd))
            .codRateThisMonth(calculateCodRate(monthStart, monthEnd))
            
            // Đơn hàng hoàn trả/hủy
            .refundedOrdersToday(orderRepository.countByDateRangeAndStatuses(todayStart, todayEnd, 
                Arrays.asList(OrderStatus.REFUNDED, OrderStatus.PARTIALLY_REFUNDED)))
            .refundedOrdersThisMonth(orderRepository.countByDateRangeAndStatuses(monthStart, monthEnd,
                Arrays.asList(OrderStatus.REFUNDED, OrderStatus.PARTIALLY_REFUNDED)))
            .canceledOrdersToday(orderRepository.countByDateRangeAndStatuses(todayStart, todayEnd, 
                Arrays.asList(OrderStatus.CANCELED)))
            .canceledOrdersThisMonth(orderRepository.countByDateRangeAndStatuses(monthStart, monthEnd,
                Arrays.asList(OrderStatus.CANCELED)))
                
            // COD thất bại
            .failedCodOrdersToday(orderRepository.countFailedCodOrdersByDateRange(todayStart, todayEnd, FAILED_COD_STATUSES))
            .failedCodOrdersThisMonth(orderRepository.countFailedCodOrdersByDateRange(monthStart, monthEnd, FAILED_COD_STATUSES))
            .failedCodRateToday(calculateFailedCodRate(todayStart, todayEnd))
            .failedCodRateThisMonth(calculateFailedCodRate(monthStart, monthEnd))
            .build();
    }

    @Override
    public RevenueChartResponse getRevenueChart(String period, Integer days) {
        log.info("Getting revenue chart for period: {}, days: {}", period, days);
        
        if (days == null) days = 30;
        
        Long startTime = getStartOfDay(-days);
        Long endTime = getEndOfDay(0);
        
        //  FIX: Sử dụng query khác nhau theo period type
        List<Object[]> rawData;
        switch (period.toLowerCase()) {
            case "weekly":
                rawData = orderRepository.findWeeklyRevenueByDateRange(startTime, endTime);

                break;
            case "monthly":
                rawData = orderRepository.findMonthlyRevenueByDateRange(startTime, endTime);
                break;
            case "daily":
            default:
                rawData = orderRepository.findDailyRevenueByDateRange(startTime, endTime);
                break;
        }
        
        List<RevenueChartResponse.RevenueDataPoint> dataPoints = rawData.stream()
            .map(row -> {
                String dateValue = row[0].toString();
                String periodDisplay = formatPeriodDisplay(dateValue, period);
                BigDecimal revenue = (BigDecimal) row[1];
                Long orderCount = ((Number) row[2]).longValue();
                
                //  NEW: Xử lý start/end date cho weekly/monthly
                String startDate = null;
                String endDate = null; 
                String dateRange = null;
                
                if (period.equalsIgnoreCase("weekly") || period.equalsIgnoreCase("monthly")) {
                    if (row.length > 4) { // Có start/end date từ query
                        startDate = row[3] != null ? row[3].toString() : null;
                        endDate = row[4] != null ? row[4].toString() : null;
                        
                        if (startDate != null && endDate != null) {
                            dateRange = formatDateRange(startDate, endDate, period);
                        }
                    }
                }
                
                return RevenueChartResponse.RevenueDataPoint.builder()
                    .date(dateValue)
                    .period(periodDisplay)
                    .revenue(revenue)
                    .orderCount(orderCount)
                    .startDate(startDate)
                    .endDate(endDate)
                    .dateRange(dateRange)
                    .build();
            })
            .collect(Collectors.toList());
        
        BigDecimal totalRevenue = dataPoints.stream()
            .map(RevenueChartResponse.RevenueDataPoint::getRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        Long totalOrders = dataPoints.stream()
            .mapToLong(RevenueChartResponse.RevenueDataPoint::getOrderCount)
            .sum();
        
        return RevenueChartResponse.builder()
            .dataPoints(dataPoints)
            .periodType(period)
            .totalRevenue(totalRevenue)
            .totalOrders(totalOrders)
            .build();
    }

    @Override
    public TopProductsResponse getTopProducts(String period, Integer limit) {
        log.info("Getting top products for period: {}, limit: {}", period, limit);
        
        if (limit == null) limit = 10;
        
        Long[] timeRange = getPeriodTimeRange(period);
        
        List<Object[]> rawData = orderRepository.findTopProductsByDateRange(
            timeRange[0], timeRange[1]);
        
        List<TopProductsResponse.ProductSalesData> topProducts = rawData.stream()
            .limit(limit)  // Apply limit in Java instead of SQL
            .map(row -> TopProductsResponse.ProductSalesData.builder()
                .bookId(((Number) row[0]).intValue())
                .bookTitle((String) row[1])
                .bookImage((String) row[2])
                .authorName((String) row[3])
                .quantitySold(((Number) row[4]).longValue())
                .totalRevenue((BigDecimal) row[5])
                .build())
            .collect(Collectors.toList());
        
        return TopProductsResponse.builder()
            .topProducts(topProducts)
            .period(period)
            .build();
    }

    @Override
    public PaymentMethodStatsResponse getPaymentMethodStats(String period) {
        log.info("Getting payment method stats for period: {}", period);
        
        Long[] timeRange = getPeriodTimeRange(period);
        
        List<Object[]> rawData = orderRepository.findPaymentMethodStatsByDateRange(
            timeRange[0], timeRange[1]);
        
        Long totalOrders = rawData.stream()
            .mapToLong(row -> ((Number) row[1]).longValue())
            .sum();
            
        BigDecimal totalAmount = rawData.stream()
            .map(row -> (BigDecimal) row[2])
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<PaymentMethodStatsResponse.PaymentMethodData> paymentMethods = rawData.stream()
            .map(row -> {
                String method = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                BigDecimal amount = (BigDecimal) row[2];
                Double percentage = totalOrders > 0 ? (count * 100.0) / totalOrders : 0.0;
                
                return PaymentMethodStatsResponse.PaymentMethodData.builder()
                    .paymentMethod(mapPaymentMethodDisplay(method))
                    .orderCount(count)
                    .totalAmount(amount)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build();
            })
            .collect(Collectors.toList());
        
        return PaymentMethodStatsResponse.builder()
            .paymentMethods(paymentMethods)
            .totalOrders(totalOrders)
            .totalAmount(totalAmount)
            .build();
    }

    @Override
    public LocationStatsResponse getLocationStats(String period) {
        log.info("Getting location stats for period: {}", period);
        
        Long[] timeRange = getPeriodTimeRange(period);
        
        List<Object[]> rawData = orderRepository.findLocationStatsByDateRange(
            timeRange[0], timeRange[1]);
        
        Long totalOrders = rawData.stream()
            .mapToLong(row -> ((Number) row[2]).longValue())
            .sum();
            
        BigDecimal totalAmount = rawData.stream()
            .map(row -> (BigDecimal) row[3])
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // Tìm max order count để tính intensity cho heatmap
        Long maxOrders = rawData.stream()
            .mapToLong(row -> ((Number) row[2]).longValue())
            .max().orElse(1L);
        
        List<LocationStatsResponse.LocationData> provinces = rawData.stream()
            .map(row -> {
                String provinceName = (String) row[0];
                Integer provinceId = row[1] != null ? ((Number) row[1]).intValue() : null;
                Long orderCount = ((Number) row[2]).longValue();
                BigDecimal amount = (BigDecimal) row[3];
                Double percentage = totalOrders > 0 ? (orderCount * 100.0) / totalOrders : 0.0;
                Double intensity = maxOrders > 0 ? (orderCount * 1.0) / maxOrders : 0.0;
                
                return LocationStatsResponse.LocationData.builder()
                    .provinceName(provinceName)
                    .provinceId(provinceId)
                    .orderCount(orderCount)
                    .totalAmount(amount)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .intensity(Math.round(intensity * 100.0) / 100.0)
                    .build();
            })
            .collect(Collectors.toList());
        
        return LocationStatsResponse.builder()
            .provinces(provinces)
            .totalOrders(totalOrders)
            .totalAmount(totalAmount)
            .build();
    }

    @Override
    public RevenueComparisonResponse getRevenueComparison() {
        log.info("Getting revenue comparison");
        
        // Tuần này
        Long thisWeekStart = getStartOfWeek(0);
        Long thisWeekEnd = getEndOfWeek(0);
        
        // Tuần trước
        Long lastWeekStart = getStartOfWeek(-1);
        Long lastWeekEnd = getEndOfWeek(-1);
        
        // Tháng này
        Long thisMonthStart = getStartOfMonth(0);
        Long thisMonthEnd = getEndOfMonth(0);
        
        // Tháng trước
        Long lastMonthStart = getStartOfMonth(-1);
        Long lastMonthEnd = getEndOfMonth(-1);
        
        BigDecimal currentWeekRevenue = orderRepository.sumRevenueByDateRangeAndStatuses(thisWeekStart, thisWeekEnd, SUCCESS_STATUSES);
        BigDecimal previousWeekRevenue = orderRepository.sumRevenueByDateRangeAndStatuses(lastWeekStart, lastWeekEnd, SUCCESS_STATUSES);
        
        BigDecimal currentMonthRevenue = orderRepository.sumRevenueByDateRangeAndStatuses(thisMonthStart, thisMonthEnd, SUCCESS_STATUSES);
        BigDecimal previousMonthRevenue = orderRepository.sumRevenueByDateRangeAndStatuses(lastMonthStart, lastMonthEnd, SUCCESS_STATUSES);
        
        Long currentWeekOrders = orderRepository.countByDateRangeAndStatuses(thisWeekStart, thisWeekEnd, SUCCESS_STATUSES);
        Long previousWeekOrders = orderRepository.countByDateRangeAndStatuses(lastWeekStart, lastWeekEnd, SUCCESS_STATUSES);
        
        Long currentMonthOrders = orderRepository.countByDateRangeAndStatuses(thisMonthStart, thisMonthEnd, SUCCESS_STATUSES);
        Long previousMonthOrders = orderRepository.countByDateRangeAndStatuses(lastMonthStart, lastMonthEnd, SUCCESS_STATUSES);
        
        return RevenueComparisonResponse.builder()
            .currentWeekRevenue(currentWeekRevenue)
            .previousWeekRevenue(previousWeekRevenue)
            .weeklyGrowthRate(calculateGrowthRate(currentWeekRevenue, previousWeekRevenue))
            .weeklyGrowthDirection(getGrowthDirection(currentWeekRevenue, previousWeekRevenue))
            
            .currentMonthRevenue(currentMonthRevenue)
            .previousMonthRevenue(previousMonthRevenue)
            .monthlyGrowthRate(calculateGrowthRate(currentMonthRevenue, previousMonthRevenue))
            .monthlyGrowthDirection(getGrowthDirection(currentMonthRevenue, previousMonthRevenue))
            
            .currentWeekOrders(currentWeekOrders)
            .previousWeekOrders(previousWeekOrders)
            .currentMonthOrders(currentMonthOrders)
            .previousMonthOrders(previousMonthOrders)
            .build();
    }

    @Override
    public CustomerStatsResponse getCustomerStats(String period) {
        log.info("Getting customer stats for period: {}", period);
        
        Long[] timeRange = getPeriodTimeRange(period);
        
        // Khách hàng mới vs quay lại
        Long newCustomers = orderRepository.countNewCustomersByDateRange(timeRange[0], timeRange[1]);
        Long returningCustomers = orderRepository.countReturningCustomersByDateRange(timeRange[0], timeRange[1]);
        Long totalCustomers = newCustomers + returningCustomers;
        Double retentionRate = totalCustomers > 0 ? (returningCustomers * 100.0) / totalCustomers : 0.0;
        
        // Top khách hàng VIP (chi tiêu nhiều)
        List<Object[]> vipRawData = orderRepository.findCustomerStatsByDateRange(timeRange[0], timeRange[1], SUCCESS_STATUSES);
        List<CustomerStatsResponse.CustomerData> vipCustomers = vipRawData.stream()
            .limit(10)
            .map(row -> CustomerStatsResponse.CustomerData.builder()
                .userId(((Number) row[0]).intValue())
                .customerName((String) row[1])
                .email((String) row[2])
                .phone((String) row[3])
                .totalOrders(((Number) row[4]).longValue())
                .totalSpent((BigDecimal) row[5])
                .customerType("vip")
                .lastOrderDate(formatTimestamp((Long) row[7]))
                .build())
            .collect(Collectors.toList());
        
        // Khách hàng rủi ro cao - tạm comment
        // List<Object[]> riskyRawData = orderRepository.findRiskyCustomersByDateRange(timeRange[0], timeRange[1]);
        List<CustomerStatsResponse.CustomerData> riskyCustomers = new ArrayList<>();
        
        return CustomerStatsResponse.builder()
            .newCustomers(newCustomers)
            .returningCustomers(returningCustomers)
            .retentionRate(Math.round(retentionRate * 100.0) / 100.0)
            .vipCustomers(vipCustomers)
            .riskyCustomers(riskyCustomers)
            .totalCustomers(totalCustomers)
            .activeCustomers(totalCustomers) // Tạm thời coi bằng total
            .build();
    }

    @Override
    public CrossSellSuggestionResponse getCrossSellSuggestions(Integer orderId, Integer limit) {
        log.info("Getting cross-sell suggestions for order: {}, limit: {}", orderId, limit);
        
        if (limit == null) limit = 5;
        
        // Implement cross-sell logic based on order details
        List<CrossSellSuggestionResponse.SuggestedProduct> suggestions = generateCrossSellSuggestions(orderId, limit);
        
        return CrossSellSuggestionResponse.builder()
            .currentOrderId(orderId)
            .suggestedProducts(suggestions)
            .suggestionType("cross_sell")
            .build();
    }
    
    private List<CrossSellSuggestionResponse.SuggestedProduct> generateCrossSellSuggestions(Integer orderId, Integer limit) {
        // Tạm thời trả về danh sách trống, có thể implement logic phức tạp:
        // 1. Lấy các sách trong đơn hàng hiện tại
        // 2. Tìm các đơn hàng khác có chứa những sách tương tự
        // 3. Gợi ý các sách thường được mua cùng
        // 4. Gợi ý sách cùng tác giả, cùng thể loại
        return new ArrayList<>();
    }
    
    // ============ PRIVATE HELPER METHODS ============
    
    //   FIXED: Tính doanh thu ròng theo CÙNG logic như summary API để đảm bảo consistency
    private BigDecimal calculateNetRevenue(Long startTime, Long endTime) {
        log.info("🔧 DEBUG: Calculating NET revenue for period {} to {} using UNIFIED TRUE logic", startTime, endTime);
        
        // ✅ SỬ DỤNG CHÍNH XÁC CÙNG LOGIC như getOrderOverview() 
        // Để đảm bảo consistency giữa Overview và Summary APIs
        return calculateTrueNetRevenue(startTime, endTime);
    }
    
    /**
     * FINAL FIX: Tính net revenue chính xác cho từng trạng thái order
     */
    private BigDecimal calculateTrueNetRevenue(Long startTime, Long endTime) {
        log.info("DEBUG: FINAL FIX - Calculating net revenue for period {} to {}", startTime, endTime);
        
        BigDecimal totalNetRevenue = BigDecimal.ZERO;
        
        // 1. Các đơn chưa hoàn trả gì: Tính full subtotal - discounts
        List<OrderStatus> fullRevenueStatuses = Arrays.asList(
            OrderStatus.DELIVERED, OrderStatus.REFUND_REQUESTED, OrderStatus.AWAITING_GOODS_RETURN,  
            OrderStatus.REFUNDING, OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER, OrderStatus.GOODS_RETURNED_TO_WAREHOUSE
        );
        
        BigDecimal fullRevenue = orderRepository.sumRevenueByDateRangeAndStatuses(startTime, endTime, fullRevenueStatuses);
        if (fullRevenue == null) fullRevenue = BigDecimal.ZERO;
        
        BigDecimal fullDiscounts = orderRepository.sumTotalDiscountsByDateRangeAndStatuses(startTime, endTime, fullRevenueStatuses);
        if (fullDiscounts == null) fullDiscounts = BigDecimal.ZERO;
        
        totalNetRevenue = totalNetRevenue.add(fullRevenue.subtract(fullDiscounts));
        
        // 2. Các đơn PARTIALLY_REFUNDED: subtotal - discounts - actual refunded amount
        List<OrderStatus> partialStatuses = Arrays.asList(OrderStatus.PARTIALLY_REFUNDED);
        
        BigDecimal partialSubtotal = orderRepository.sumRevenueByDateRangeAndStatuses(startTime, endTime, partialStatuses);
        if (partialSubtotal == null) partialSubtotal = BigDecimal.ZERO;
        
        BigDecimal partialDiscounts = orderRepository.sumTotalDiscountsByDateRangeAndStatuses(startTime, endTime, partialStatuses);
        if (partialDiscounts == null) partialDiscounts = BigDecimal.ZERO;
        
        // CHÍNH XÁC: Chỉ lấy refunded amount từ orders PARTIALLY_REFUNDED trong khoảng thời gian này
        BigDecimal partialRefunded = getRefundedAmountFromPartialOrders(startTime, endTime);
        
        BigDecimal partialNetRevenue = partialSubtotal.subtract(partialDiscounts).subtract(partialRefunded);
        totalNetRevenue = totalNetRevenue.add(partialNetRevenue);
        
        // 3. Các đơn REFUNDED: Net revenue = 0 (không tính vào)
        
        log.info("DEBUG: Full Revenue = {} - {} = {}, Partial = {} - {} - {} = {}, Total = {}", 
                fullRevenue, fullDiscounts, fullRevenue.subtract(fullDiscounts),
                partialSubtotal, partialDiscounts, partialRefunded, partialNetRevenue,
                totalNetRevenue);
        
        return totalNetRevenue;
    }
    
    /**
     * Lấy chính xác số tiền đã hoàn từ các đơn PARTIALLY_REFUNDED
     */
    private BigDecimal getRefundedAmountFromPartialOrders(Long startTime, Long endTime) {
        // Query chỉ lấy refunded amount từ orders có status = PARTIALLY_REFUNDED trong thời gian
        BigDecimal result = orderRepository.sumRefundedAmountFromPartialOrdersByDateRange(startTime, endTime);
        return result != null ? result : BigDecimal.ZERO;
    }
    
    /**
     * 🔧 PUBLIC API: Helper method để đảm bảo consistency giữa các API statistics
     * Expose calculateTrueNetRevenue logic cho OrderService sử dụng
     */
    @Override
    public BigDecimal calculateNetRevenueForPeriod(Long startTime, Long endTime) {
        return calculateTrueNetRevenue(startTime, endTime);
    }
    
    //  THÊM: Tính doanh thu trung bình trên mỗi đơn
    private BigDecimal calculateAverageRevenuePerOrder(BigDecimal totalRevenue, Long totalOrders) {
        if (totalOrders == null || totalOrders == 0) {
            return BigDecimal.ZERO;
        }
        return totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateNetProfit(Long startTime, Long endTime) {
        //  SỬA: Sử dụng doanh thu ròng thay vì gross revenue
        BigDecimal netRevenue = calculateNetRevenue(startTime, endTime);
        BigDecimal shippingCost = orderRepository.sumShippingFeeByDateRangeAndStatuses(startTime, endTime, SUCCESS_STATUSES);
        // Tạm thời tính lợi nhuận = doanh thu ròng - phí ship (có thể mở rộng thêm chi phí khác)
        return netRevenue.subtract(shippingCost);
    }
    
    private Double calculateCodRate(Long startTime, Long endTime) {
        Long totalOrders = orderRepository.countByDateRangeAndStatuses(startTime, endTime, SUCCESS_STATUSES);
        Long codOrders = orderRepository.countCodOrdersByDateRangeAndStatuses(startTime, endTime, SUCCESS_STATUSES);
        
        if (totalOrders == 0) return 0.0;
        return Math.round((codOrders * 100.0 / totalOrders) * 100.0) / 100.0;
    }
    
    private Double calculateFailedCodRate(Long startTime, Long endTime) {
        Long totalCodOrders = orderRepository.countCodOrdersByDateRangeAndStatuses(startTime, endTime, 
            Arrays.asList(OrderStatus.values())); // Tất cả trạng thái
        Long failedCodOrders = orderRepository.countFailedCodOrdersByDateRange(startTime, endTime, FAILED_COD_STATUSES);
        
        if (totalCodOrders == 0) return 0.0;
        return Math.round((failedCodOrders * 100.0 / totalCodOrders) * 100.0) / 100.0;
    }
    
    private String formatPeriodDisplay(String dateStr, String period) {
        //  FIX: Format hiển thị theo period type
        switch (period.toLowerCase()) {
            case "weekly":
                // dateStr format: "2025-W32" -> "Tuần 32, 2025"
                if (dateStr.contains("-W")) {
                    String[] parts = dateStr.split("-W");
                    return String.format("Tuần %s, %s", parts[1], parts[0]);
                }
                return dateStr;
            case "monthly":
                // dateStr format: "2025-07" -> "Tháng 7, 2025"
                if (dateStr.matches("\\d{4}-\\d{2}")) {
                    String[] parts = dateStr.split("-");
                    return String.format("Tháng %s, %s", Integer.parseInt(parts[1]), parts[0]);
                }
                return dateStr;
            case "daily":
            default:
                // dateStr format: "2025-07-08" -> "08/07/2025"
                if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    String[] parts = dateStr.split("-");
                    return String.format("%s/%s/%s", parts[2], parts[1], parts[0]);
                }
                return dateStr;
        }
    }
    
    private String formatDateRange(String startDate, String endDate, String period) {
        //  NEW: Format khoảng thời gian đẹp cho frontend
        try {
            if (startDate == null || endDate == null) return null;
            
            switch (period.toLowerCase()) {
                case "weekly":
                    // "2025-07-01" & "2025-07-07" -> "01/07 - 07/07"
                    String[] startParts = startDate.split("-");
                    String[] endParts = endDate.split("-");
                    return String.format("%s/%s - %s/%s", 
                        startParts[2], startParts[1], 
                        endParts[2], endParts[1]);
                        
                case "monthly":
                    // "2025-07-01" & "2025-07-31" -> "01/07 - 31/07"
                    String[] monthStartParts = startDate.split("-");
                    String[] monthEndParts = endDate.split("-");
                    return String.format("%s/%s - %s/%s", 
                        monthStartParts[2], monthStartParts[1], 
                        monthEndParts[2], monthEndParts[1]);
                        
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Error formatting date range: {} - {}", startDate, endDate, e);
            return null;
        }
    }
    
    private String mapPaymentMethodDisplay(String method) {
        return switch (method) {
            case "COD" -> "Thanh toán khi nhận hàng";
            case "ONLINE" -> "Thanh toán trực tuyến";
            default -> method;
        };
    }
    
    private Long[] getPeriodTimeRange(String period) {
        return switch (period) {
            case "today" -> new Long[]{getStartOfDay(0), getEndOfDay(0)};
            case "week" -> new Long[]{getStartOfWeek(0), getEndOfWeek(0)};
            case "month" -> new Long[]{getStartOfMonth(0), getEndOfMonth(0)};
            default -> new Long[]{getStartOfWeek(0), getEndOfWeek(0)};
        };
    }
    
    private Double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        BigDecimal growth = current.subtract(previous)
            .divide(previous, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        return Math.round(growth.doubleValue() * 100.0) / 100.0;
    }
    
    private String getGrowthDirection(BigDecimal current, BigDecimal previous) {
        int comparison = current.compareTo(previous);
        if (comparison > 0) return "up";
        if (comparison < 0) return "down";
        return "same";
    }
    
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        ).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    // Time range helper methods
    private Long getStartOfDay(int dayOffset) {
        return LocalDate.now().plusDays(dayOffset)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli();
    }
    
    private Long getEndOfDay(int dayOffset) {
        return LocalDate.now().plusDays(dayOffset)
            .atTime(23, 59, 59, 999_999_999)
            .atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli();
    }
    
    private Long getStartOfWeek(int weekOffset) {
        return LocalDate.now()
            .with(java.time.DayOfWeek.MONDAY)
            .plusWeeks(weekOffset)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli();
    }
    
    private Long getEndOfWeek(int weekOffset) {
        return LocalDate.now()
            .with(java.time.DayOfWeek.SUNDAY)
            .plusWeeks(weekOffset)
            .atTime(23, 59, 59, 999_999_999)
            .atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli();
    }
    
    private Long getStartOfMonth(int monthOffset) {
        return LocalDate.now()
            .withDayOfMonth(1)
            .plusMonths(monthOffset)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli();
    }
    
    private Long getEndOfMonth(int monthOffset) {
        return LocalDate.now()
            .withDayOfMonth(1)
            .plusMonths(monthOffset + 1)
            .minusDays(1)
            .atTime(23, 59, 59, 999_999_999)
            .atZone(ZoneId.systemDefault())
            .toInstant().toEpochMilli();
    }
}
