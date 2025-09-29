package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatsFilterRequest {
    
    // 📅 THỜI GIAN
    private Long startDate;                     // Timestamp bắt đầu
    private Long endDate;                       // Timestamp kết thúc
    
    @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|QUARTERLY|YEARLY", message = "Period type must be DAILY, WEEKLY, MONTHLY, QUARTERLY, or YEARLY")
    private String periodType;                  // Loại kỳ thống kê
    
    // 🎯 LỌC THEO THUỘC TÍNH
    private Integer categoryId;                 // Lọc theo danh mục
    private Integer authorId;                   // Lọc theo tác giả
    private Integer publisherId;                // Lọc theo nhà xuất bản
    private Integer supplierId;                 // Lọc theo nhà cung cấp
    
    // 💰 LỌC THEO GIÁ
    private java.math.BigDecimal minPrice;      // Giá tối thiểu
    private java.math.BigDecimal maxPrice;      // Giá tối đa
    
    // 📊 LỌC THEO HIỆU SUẤT
    @Pattern(regexp = "ALL|TOP_PERFORMERS|AVERAGE_PERFORMERS|POOR_PERFORMERS|RISING_STARS|DECLINING", 
             message = "Performance level must be valid")
    private String performanceLevel;            // Mức độ hiệu suất
    
    @Pattern(regexp = "ALL|IN_STOCK|LOW_STOCK|OUT_OF_STOCK|OVERSTOCKED", 
             message = "Stock status must be valid")
    private String stockStatus;                 // Trạng thái kho
    
    // 🔍 LỌC NÂNG CAO
    private Boolean hasDiscount;                // Có giảm giá không
    private Boolean inFlashSale;                // Có trong flash sale không
    private Boolean hasReviews;                 // Có đánh giá không
    private Double minRating;                   // Đánh giá tối thiểu
    
    // 📈 SẮP XẾP & PHÂN TRANG
    @Pattern(regexp = "revenue|sales|rating|stock|name|date", message = "Sort by must be valid field")
    @Builder.Default
    private String sortBy = "revenue";          // Sắp xếp theo
    
    @Pattern(regexp = "ASC|DESC", message = "Sort direction must be ASC or DESC")
    @Builder.Default
    private String sortDirection = "DESC";      // Hướng sắp xếp
    
    @Min(value = 0, message = "Page must be non-negative")
    @Builder.Default
    private Integer page = 0;                   // Trang hiện tại
    
    @Min(value = 1, message = "Size must be positive")
    @Builder.Default
    private Integer size = 10;                  // Kích thước trang
    
    // 🎯 TÙYCHỌN PHÂN TÍCH
    private Boolean includeProjections;         // Bao gồm dự báo
    private Boolean includeComparisons;         // Bao gồm so sánh
    private Boolean includeInsights;            // Bao gồm insights
    private Boolean includeRecommendations;     // Bao gồm khuyến nghị
}
