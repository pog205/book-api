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
public class BookCustomerInsightResponse {
    // 👥 THỐNG KÊ KHÁCH HÀNG TỔNG QUAN
    private Long totalCustomers;                // Tổng số khách hàng đã mua sách
    private Long newCustomersLast30Days;        // Khách hàng mới trong 30 ngày
    private Long returningCustomers;            // Khách hàng quay lại mua
    private Double customerRetentionRate;       // Tỷ lệ giữ chân khách hàng
    
    // 💰 PHÂN TÍCH GIÁ TRỊ KHÁCH HÀNG
    private BigDecimal averageCustomerValue;    // Giá trị khách hàng trung bình
    private BigDecimal totalCustomerValue;      // Tổng giá trị khách hàng
    private Long averageOrdersPerCustomer;      // Số đơn hàng TB/khách hàng
    private BigDecimal averageOrderValue;       // Giá trị đơn hàng trung bình
    
    // 🎯 PHÂN KHÚC KHÁCH HÀNG
    private Long vipCustomers;                  // Khách hàng VIP (high-value)
    private Long regularCustomers;              // Khách hàng thường xuyên
    private Long occasionalCustomers;           // Khách hàng thỉnh thoảng
    private Long oneTimeCustomers;              // Khách hàng mua 1 lần
    
    // 📊 HÀNH VI MUA HÀNG
    private Double averageBooksPerOrder;        // Số sách trung bình/đơn hàng
    private String popularPurchaseTime;         // Thời điểm mua phổ biến
    private String preferredPaymentMethod;      // Phương thức thanh toán ưa thích
    private Double averageTimeBetweenOrders;    // Thời gian TB giữa các đơn
    
    // 🌟 SỞ THÍCH & HÀNH VI
    private String mostPopularCategory;         // Danh mục được ưa thích nhất
    private String mostPopularAuthor;           // Tác giả được ưa thích nhất
    private BigDecimal averagePreferredPrice;   // Mức giá ưa thích trung bình
    private String preferredBookFormat;         // Định dạng sách ưa thích
    
    // 📈 THỐNG KÊ TĂNG TRƯỞNG
    private Double customerGrowthRate;          // Tỷ lệ tăng trưởng khách hàng
    private Double revenueGrowthFromBooks;      // Tăng trưởng doanh thu từ sách
    private Long customersWithMultipleOrders;   // KH có nhiều đơn hàng
    private Double repeatPurchaseRate;          // Tỷ lệ mua lặp lại
    
    // 🎁 THỐNG KÊ CHƯƠNG TRÌNH KHUYẾN MÃI
    private Long customersUsingVouchers;        // KH sử dụng voucher
    private BigDecimal totalDiscountUsed;       // Tổng giảm giá đã sử dụng
    private Double voucherUsageRate;            // Tỷ lệ sử dụng voucher
    private String mostEffectivePromotion;      // Chương trình KM hiệu quả nhất
    
    // 🔍 INSIGHTS QUAN TRỌNG
    private String customerLoyaltyLevel;        // Mức độ trung thành KH
    private Double churnRisk;                   // Nguy cơ khách hàng rời bỏ
    private String recommendedCustomerStrategy; // Chiến lược KH khuyến nghị
    private Boolean needsCustomerAcquisition;   // Cần thu hút KH mới
    private Boolean needsRetentionProgram;      // Cần chương trình giữ chân
}
