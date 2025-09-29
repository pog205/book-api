package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendingBookResponse {
    // Basic book info
    private Integer id;
    private String bookName;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice; // Giá gốc (nếu có discount)
    private Integer discountPercentage; // % giảm giá
    private Integer stockQuantity;
    private String imageUrl;
    private String bookCode;
    private Long publicationDate;
    
    // Trạng thái giảm giá có đang kích hoạt không
    private Boolean discountActive;
    // Category info
    private Integer categoryId;
    private String categoryName;
    
    // Authors info
    private List<AuthorResponse> authors;
    
    // Supplier info
    private Integer supplierId;
    private String supplierName;
    
    // Review/Rating info
    private Double rating; // Rating trung bình
    private Integer reviewCount; // Số lượng review
    
    // Sales info
    private Integer soldCount; // Số lượng đã bán trong 30 ngày
    private Integer orderCount; // Số đơn hàng trong 30 ngày
    
    // Trending info
    private Double trendingScore; // Điểm trending (0-10)
    private Integer trendingRank; // Thứ hạng trong danh sách trending
    
    // Flash sale info
    private Boolean isInFlashSale; // Có đang trong flash sale không
    private BigDecimal flashSalePrice; // Giá flash sale
    private Integer flashSaleStockQuantity; // Số lượng còn lại trong flash sale
    private Integer flashSaleSoldCount; // ✅ Số lượng đã bán riêng trong flash sale
    
    // Timestamps
    private Long createdAt;
    private Long updatedAt;
    
    // ✅ THÊM MỚI: Danh sách ảnh sản phẩm (nhiều ảnh)
    private List<String> images;
}
