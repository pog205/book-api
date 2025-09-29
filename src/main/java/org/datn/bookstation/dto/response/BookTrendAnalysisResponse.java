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
public class BookTrendAnalysisResponse {
    private String period;                      // Kỳ thống kê (2024-01, 2024-Q1, 2024)
    private String periodType;                  // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    
    // 📊 THỐNG KÊ BÁN HÀNG
    private Long totalBooksSold;                // Tổng số sách bán ra
    private Long totalOrders;                   // Tổng số đơn hàng
    private BigDecimal totalRevenue;            // Tổng doanh thu
    private Long uniqueCustomers;               // Số khách hàng duy nhất
    
    // 📈 SO SÁNH VỚI KỲ TRƯỚC
    private Double salesGrowthRate;             // Tỷ lệ tăng trưởng bán hàng (%)
    private Double revenueGrowthRate;           // Tỷ lệ tăng trưởng doanh thu (%)
    private Double customerGrowthRate;          // Tỷ lệ tăng trưởng khách hàng (%)
    
    // 🔥 TOP PERFORMERS
    private String topSellingBookName;          // Tên sách bán chạy nhất
    private Long topSellingBookId;              // ID sách bán chạy nhất
    private Long topSellingQuantity;            // Số lượng bán của sách top
    
    // 📚 PHÂN TÍCH DANH MỤC
    private String topCategoryName;             // Danh mục bán chạy nhất
    private Long topCategoryId;                 // ID danh mục bán chạy nhất
    private Long topCategorySales;              // Doanh số danh mục top
    
    // ⭐ CHẤT LƯỢNG DỊCH VỤ
    private Double averageRating;               // Đánh giá trung bình
    private Long totalReviews;                  // Tổng số đánh giá
    private Double satisfactionRate;            // Tỷ lệ hài lòng (4-5 sao)
    
    // 💰 PHÂN TÍCH GIA
    private BigDecimal averageOrderValue;       // Giá trị đơn hàng trung bình
    private BigDecimal averageBookPrice;        // Giá sách trung bình
    private BigDecimal totalDiscountGiven;      // Tổng giảm giá đã áp dụng
    
    // 🎯 INSIGHTS
    private String trendStatus;                 // GROWING, STABLE, DECLINING
    private String seasonalPattern;             // HIGH_SEASON, LOW_SEASON, NORMAL
    private String marketHealthScore;           // EXCELLENT, GOOD, AVERAGE, POOR
}
