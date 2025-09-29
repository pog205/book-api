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
public class BookComparisonResponse {
    
    // 📊 THÔNG TIN SO SÁNH
    private String comparisonType;              // "BOOK_VS_BOOK", "BOOK_VS_ALL"
    private BookComparisonData book1;           // Dữ liệu sách 1
    private BookComparisonData book2;           // Dữ liệu sách 2 (null nếu so sánh với tất cả)
    private BookComparisonData allBooksAverage; // Dữ liệu trung bình tất cả sách (khi so sánh với tất cả)
    
    // 📈 KẾT LUẬN SO SÁNH
    private ComparisonInsight insight;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookComparisonData {
        private Integer bookId;
        private String bookName;
        private BigDecimal price;
        private Long totalSold;                 // Tổng số đã bán
        private BigDecimal totalRevenue;        // Doanh thu
        private Double averageRating;           // Điểm đánh giá
        private Long totalReviews;              // Số lượng đánh giá
        private Long stockQuantity;             // Tồn kho
        private Double salesVelocity;           // Tốc độ bán (sold/ngày)
        private Double growthRate;              // Tăng trưởng so với tháng trước (%)
        private String performanceLevel;        // "EXCELLENT", "GOOD", "AVERAGE", "POOR"
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonInsight {
        private String betterPerformer;         // Tên sách có hiệu suất tốt hơn
        private String reasonWhy;               // Lý do tại sao
        private Double performanceDifference;   // Chênh lệch hiệu suất (%)
        private String recommendation;          // Khuyến nghị
    }
}
