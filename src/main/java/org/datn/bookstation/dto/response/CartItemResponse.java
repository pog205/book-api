package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartItemResponse {
    private Integer id;
    private Integer cartId;
    private Integer bookId;
    private String bookName;
    private String bookCode;
    private String bookImageUrl;
    private BigDecimal bookPrice; // Giá gốc của sách
    
    // Flash sale info
    private Integer flashSaleItemId;
    private String flashSaleName;
    private BigDecimal flashSalePrice;
    private BigDecimal flashSaleDiscount; // % giảm giá
    private Long flashSaleEndTime;
    private boolean isFlashSaleExpired;
    
    // Cart item details
    private Integer quantity;
    private BigDecimal unitPrice; // Giá cuối cùng (flash sale hoặc regular)
    private BigDecimal totalPrice; // unitPrice * quantity
    
    // Stock validation
    private Integer availableStock; // Stock hiện tại (book hoặc flashSaleItem)
    private boolean isOutOfStock; // quantity > availableStock
    private boolean isStockLimited; // availableStock < quantity
    private Integer maxAvailableQuantity;
    
    // Metadata
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private Byte status;
    
    // UI helpers
    private String itemType; // "REGULAR" hoặc "FLASH_SALE"
    private String stockWarning; // "Chỉ còn 2 sản phẩm"
    private boolean canAddMore; // có thể tăng quantity không
    // NEW: Trạng thái chọn/bỏ trên UI
    private Boolean selected;
}
