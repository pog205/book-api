package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 📊 Response cho sách với thông tin sentiment chi tiết
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSentimentResponse {
    
    // 📚 Thông tin cơ bản của sách (copy từ BookResponse)
    private Integer id;
    private String bookName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Long publicationDate;
    private String categoryName;
    private Integer categoryId;
    private String supplierName;
    private Integer supplierId;
    private String bookCode;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
    
    // Thông tin tác giả và nhà xuất bản
    private List<AuthorResponse> authors;
    private String publisherName;
    private Integer publisherId;
    
    // Thông tin ảnh và chi tiết sách
    private String coverImageUrl;
    private String translator;
    private String isbn;
    private Integer pageCount;
    private String language;
    private Integer weight;
    private String dimensions;
    private List<String> images;
    
    // Thông tin bán hàng và khuyến mãi
    private Integer soldCount;
    private Integer processingQuantity;
    private BigDecimal discountValue;
    private Integer discountPercent;
    private Boolean discountActive;
    
    // Flash Sale info
    private Boolean isInFlashSale;
    private BigDecimal flashSalePrice;
    private Integer flashSaleStock;
    private Integer flashSaleSoldCount;
    private Long flashSaleEndTime;
    
    // 📊 **THÔNG TIN SENTIMENT CHI TIẾT**
    private SentimentStats sentimentStats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentStats {
        // 📈 Tỉ lệ đánh giá tích cực (chính xác)
        private Double positivePercentage;
        
        // ⭐ Điểm sao trung bình
        private Double averageRating;
        
        // 📊 Thống kê chi tiết
        private Integer totalReviews;
        private Integer positiveReviews;
        private Integer negativeReviews;
        
        // 📋 Phân bố rating
        private RatingDistribution ratingDistribution;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingDistribution {
        private Integer rating1Count;
        private Integer rating2Count;
        private Integer rating3Count;
        private Integer rating4Count;
        private Integer rating5Count;
    }
}
