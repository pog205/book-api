package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DropdownOptionResponse {
    private Integer id;
    private String name;
    private BigDecimal normalPrice;
    private BigDecimal flashSalePrice;
    private Boolean isFlashSale;

    // Bổ sung các trường mới
    private String bookCode;
    private Integer stockQuantity;
    private Integer soldQuantity;
    private Integer flashSaleSoldQuantity;
    private BigDecimal originalPrice;

    // ✅ THÊM MỚI: Số lượng đang xử lý
    private Integer processingQuantity;
    private Integer flashSaleProcessingQuantity;
    private Integer flashSaleStockQuantity; // Số lượng tồn kho flash sale

    // ✅ THÊM MỚI: Ảnh sản phẩm
    private String imageUrl; // Cover image URL

    // Constructor cũ để backward compatibility
    public DropdownOptionResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.isFlashSale = false;
    }

    // ✅ THÊM MỚI: Constructor với imageUrl
    public DropdownOptionResponse(Integer id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.isFlashSale = false;
    }
}
