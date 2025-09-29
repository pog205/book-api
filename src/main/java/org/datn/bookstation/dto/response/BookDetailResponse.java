package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDetailResponse {
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
    
    // ✅ Thông tin chi tiết
    private List<AuthorResponse> authors;
    private String publisherName;
    private Integer publisherId;
    private String coverImageUrl;
    private String translator;
    private String isbn;
    private Integer pageCount;
    private String language;
    private Integer weight;
    private String dimensions;
    
    // 🔥 Flash Sale info (đơn giản)
    private BigDecimal flashSalePrice;      // Giá flash sale (null nếu không có)
    private Integer flashSaleStock;         // ✅ THÊM: Số lượng flash sale còn lại  
    private BigDecimal flashSaleDiscount;   // % giảm giá
    private Long flashSaleEndTime;          // Timestamp kết thúc
    private Integer flashSaleSoldCount;     // Đã bán bao nhiêu
    
    // 🔥 Direct Discount info (giảm giá cố định của book)
    private BigDecimal discountValue;       // Giảm theo giá trị (null nếu không có)
    private Integer discountPercent;        // Giảm theo phần trăm (null nếu không có)
    
    private Long serverTime;                // 🔥 Thời gian server hiện tại (chống hack client)
    
    // ✅ THÊM MỚI: Danh sách ảnh sản phẩm (nhiều ảnh)
    private List<String> images;
}
