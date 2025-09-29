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
public class BookCategoryStatsResponse {
    private Integer categoryId;
    private String categoryName;
    private String categoryDescription;
    
    // 📚 THỐNG KÊ SẢN PHẨM
    private Long totalBooks;                    // Tổng số sách trong danh mục
    private Long activeBooks;                   // Số sách đang hoạt động
    private Long outOfStockBooks;               // Số sách hết hàng
    private BigDecimal totalInventoryValue;     // Tổng giá trị kho
    
    // 💰 THỐNG KÊ DOANH THU
    private BigDecimal totalRevenue;            // Tổng doanh thu danh mục
    private BigDecimal averageBookPrice;        // Giá trung bình sách trong danh mục
    private Double revenueSharePercentage;      // Tỷ lệ đóng góp doanh thu (%)
    private BigDecimal averageOrderValue;       // Giá trị đơn hàng trung bình
    
    // 📈 THỐNG KÊ BÁN HÀNG
    private Long totalBooksSold;                // Tổng số sách đã bán
    private Long totalOrders;                   // Tổng số đơn hàng
    private Double averageSalesPerBook;         // Trung bình bán/cuốn
    private Double conversionRate;              // Tỷ lệ chuyển đổi
    
    // ⭐ THỐNG KÊ CHẤT LƯỢNG
    private Long totalReviews;                  // Tổng số đánh giá
    private Double averageRating;               // Điểm đánh giá trung bình
    private Double satisfactionRate;            // Tỷ lệ hài lòng
    private Long returnsAndRefunds;             // Số lượng trả hàng/hoàn tiền
    
    // 🔥 HIỆU SUẤT
    private String performanceLevel;            // EXCELLENT, GOOD, AVERAGE, POOR
    private Double growthRate;                  // Tỷ lệ tăng trưởng so với kỳ trước
    private Integer rankAmongCategories;        // Thứ hạng trong tất cả danh mục
    
    // 🎯 TOP PERFORMERS TRONG DANH MỤC
    private String topSellingBookName;          // Sách bán chạy nhất
    private Integer topSellingBookId;           // ID sách bán chạy nhất
    private Long topSellingQuantity;            // Số lượng bán của sách top
    private String topRatedBookName;            // Sách đánh giá cao nhất
    private Double topRatedBookRating;          // Điểm đánh giá của sách top
    
    // 📊 INSIGHTS ĐÁNG CHÚ Ý
    private Boolean isTopPerformingCategory;    // Có phải danh mục hàng đầu
    private Boolean needsInventoryRestock;      // Cần bổ sung hàng
    private Boolean hasHighReturnRate;          // Có tỷ lệ trả hàng cao
    private String recommendedAction;           // Khuyến nghị hành động
}
