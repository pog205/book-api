package org.datn.bookstation.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BookRequest {
    @NotBlank(message = "Tên sách không được để trống")
    private String bookName;
    
    private String description;
    
    @NotNull(message = "Giá sách không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sách phải lớn hơn 0")
    private BigDecimal price;
    
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải >= 0")
    private Integer stockQuantity;
    
    private Long publicationDate;
    private Integer categoryId;
    private Integer supplierId;
    private String bookCode;
    private Byte status;
    
    // ✅ THÊM MỚI: Danh sách ID tác giả
    @NotEmpty(message = "Sách phải có ít nhất một tác giả")
    private List<Integer> authorIds;
    
    // ✅ THÊM MỚI: Nhà xuất bản
    private Integer publisherId;
    
    // ✅ THÊM MỚI: Ảnh bìa sách
    private String coverImageUrl;

    // ✅ THÊM MỚI: Danh sách ảnh chính (nhiều ảnh)
    private List<String> images;
    
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
    // ✅ THÊM MỚI: Trường giảm giá cho sách
    private java.math.BigDecimal discountValue; // Giảm giá theo số tiền
    private Integer discountPercent; // Giảm giá theo %
    private Boolean discountActive; // Có áp dụng discount không
}
