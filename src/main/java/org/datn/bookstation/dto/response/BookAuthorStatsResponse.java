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
public class BookAuthorStatsResponse {
    private Integer authorId;
    private String authorName;
    private String authorBiography;
    
    // 📚 THỐNG KÊ SẢN PHẨM
    private Long totalBooks;                    // Tổng số sách của tác giả
    private Long activeBooks;                   // Số sách đang bán
    private Long totalGenres;                   // Số thể loại sách viết
    
    // 💰 THỐNG KÊ DOANH THU
    private BigDecimal totalRevenue;            // Tổng doanh thu từ tác giả
    private BigDecimal averageBookPrice;        // Giá trung bình sách tác giả
    private Double revenueContributionPercentage; // Tỷ lệ đóng góp doanh thu (%)
    
    // 📈 THỐNG KÊ BÁN HÀNG
    private Long totalBooksSold;                // Tổng số sách đã bán
    private Long totalOrders;                   // Tổng đơn hàng chứa sách tác giả
    private Double averageSalesPerBook;         // Trung bình bán/cuốn
    private Long totalCustomers;                // Số khách hàng mua sách tác giả
    
    // ⭐ THỐNG KÊ CHẤT LƯỢNG
    private Long totalReviews;                  // Tổng số đánh giá
    private Double averageRating;               // Điểm đánh giá trung bình
    private Double satisfactionRate;            // Tỷ lệ hài lòng
    private Long fiveStarReviews;               // Số đánh giá 5 sao
    
    // 🔥 HIỆU SUẤT & XẾP HẠNG
    private Integer rankAmongAuthors;           // Thứ hạng trong tất cả tác giả
    private String popularityLevel;             // BESTSELLER, POPULAR, AVERAGE, NICHE
    private Double growthRate;                  // Tỷ lệ tăng trưởng bán hàng
    
    // 🎯 TOP PERFORMERS
    private String bestSellingBookName;         // Sách bán chạy nhất
    private Integer bestSellingBookId;          // ID sách bán chạy nhất
    private Long bestSellingQuantity;           // Số lượng bán của sách top
    private String highestRatedBookName;        // Sách đánh giá cao nhất
    private Double highestRatedBookRating;      // Điểm đánh giá cao nhất
    
    // 📊 PHÂN TÍCH ĐẶC BIỆT
    private Boolean isTopSellingAuthor;         // Có phải tác giả bán chạy
    private Boolean isRisingAuthor;             // Có phải tác giả đang lên
    private Boolean hasConsistentQuality;       // Có chất lượng đồng đều
    private String mostPopularGenre;            // Thể loại phổ biến nhất
    private String fanbaseProfile;              // Hồ sơ người hâm mộ
    private String recommendedStrategy;         // Chiến lược khuyến nghị
}
