package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartSummaryResponse {
    private Integer cartId;
    private Integer userId;
    private Integer totalItems; // Tổng số items
    private Integer totalQuantity; // Tổng số sách (sum của quantity)
    
    // Pricing summary
    private BigDecimal totalAmount; // Tổng tiền cuối cùng
    private BigDecimal totalRegularAmount; // Tổng tiền sách thường  
    private BigDecimal totalFlashSaleAmount; // Tổng tiền flash sale
    private BigDecimal totalSavings; // Tiền tiết kiệm được từ flash sale
    
    // Item breakdown
    private Integer regularItemsCount;
    private Integer flashSaleItemsCount;
    
    // Status flags
    private boolean hasItems;
    private boolean hasOutOfStockItems;
    private boolean hasExpiredFlashSaleItems;
    private boolean readyForCheckout; // Tất cả items đều OK
    
    // Messages
    private String statusMessage; // "Giỏ hàng trống" hoặc "Sẵn sàng thanh toán"
    private String warningMessage; // "Có 2 sản phẩm hết hàng"
}
