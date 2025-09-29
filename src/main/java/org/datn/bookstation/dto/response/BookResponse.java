package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
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
    
    // ✅ THÊM MỚI: Danh sách tác giả
    private List<AuthorResponse> authors;
    
    // ✅ THÊM MỚI: Nhà xuất bản
    private String publisherName;
    private Integer publisherId;
    
    // ✅ THÊM MỚI: Ảnh bìa sách
    private String coverImageUrl;
    
    // ✅ THÊM MỚI: Người dịch
    private String translator;
    
    // ✅ THÊM MỚI: ISBN
    private String isbn;
    
    // ✅ THÊM MỚI: Số trang
    private Integer pageCount;
    
    // ✅ THÊM MỚI: Ngôn ngữ
    private String language;
    
    // ✅ THÊM MỚI: Cân nặng (gram)
    private Integer weight;
    
    // ✅ THÊM MỚI: Kích thước (dài x rộng x cao) cm
    private String dimensions;
    
    // ✅ THÊM MỚI: Danh sách ảnh sản phẩm (nhiều ảnh)
    private List<String> images;
    
    // ✅ ADMIN CẦN: Thông tin đã bán và Flash Sale
    private Integer soldCount; // Số lượng đã bán tổng cộng
    private Integer processingQuantity; // ✅ THÊM MỚI: Số lượng đang xử lý (real-time)
    private BigDecimal discountValue; // Giảm giá trực tiếp (VD: 50,000 VND)
    private Integer discountPercent; // Giảm giá theo % (VD: 20%)
    private Boolean discountActive; // Trạng thái giảm giá
    
    // ✅ Flash Sale info (nếu đang có)
    private Boolean isInFlashSale; // Có đang trong flash sale không
    private BigDecimal flashSalePrice; // Giá flash sale 
    private Integer flashSaleStock; // ✅ THÊM: Số lượng flash sale còn lại
    private Integer flashSaleSoldCount; // Đã bán trong flash sale
    private Long flashSaleEndTime; // Thời gian kết thúc flash sale
}
