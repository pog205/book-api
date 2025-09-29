package org.datn.bookstation.dto.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho việc tạo đơn hàng từ checkout session
 * Bao gồm validation giá để chống price manipulation
 */
@Getter
@Setter
public class CheckoutSessionOrderRequest {
    
    @NotNull(message = "Session ID không được để trống")
    private Integer sessionId;
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    // ✅ THÊM MỚI: Giá frontend gửi lên để validate với backend
    private List<ItemPriceValidation> expectedPrices;
    
    // ✅ THÊM MỚI: Tổng tiền frontend tính được để validate
    private BigDecimal expectedSubtotal;
    private BigDecimal expectedTotal;
    
    @Getter
    @Setter
    public static class ItemPriceValidation {
        @NotNull
        private Integer bookId;
        
        @NotNull
        private Integer quantity;
        
        @NotNull
        private BigDecimal expectedUnitPrice; // Giá frontend tính được
        
        // Flash sale info frontend biết
        private Integer expectedFlashSaleId;
        private BigDecimal expectedFlashSalePrice;
    }
}
