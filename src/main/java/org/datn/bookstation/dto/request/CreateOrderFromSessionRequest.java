package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho việc tạo order từ session với price validation
 */
public class CreateOrderFromSessionRequest {
    
    @NotNull(message = "Danh sách giá sản phẩm frontend đang hiển thị là bắt buộc")
    private List<ItemPriceValidation> frontendPrices;
    
    /**
     * Inner class chứa thông tin giá mà frontend đang hiển thị
     */
    public static class ItemPriceValidation {
        @NotNull(message = "Book ID không được để trống")
        private Integer bookId;
        
        @NotNull(message = "Số lượng không được để trống")
        private Integer quantity;
        
        @NotNull(message = "Unit price frontend đang hiển thị không được để trống")
        private BigDecimal frontendUnitPrice;
        
        // Flash sale info nếu có
        private Long frontendFlashSaleId;
        private BigDecimal frontendFlashSalePrice;
        
        // Getters and setters
        public Integer getBookId() {
            return bookId;
        }

        public void setBookId(Integer bookId) {
            this.bookId = bookId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getFrontendUnitPrice() {
            return frontendUnitPrice;
        }

        public void setFrontendUnitPrice(BigDecimal frontendUnitPrice) {
            this.frontendUnitPrice = frontendUnitPrice;
        }

        public Long getFrontendFlashSaleId() {
            return frontendFlashSaleId;
        }

        public void setFrontendFlashSaleId(Long frontendFlashSaleId) {
            this.frontendFlashSaleId = frontendFlashSaleId;
        }

        public BigDecimal getFrontendFlashSalePrice() {
            return frontendFlashSalePrice;
        }

        public void setFrontendFlashSalePrice(BigDecimal frontendFlashSalePrice) {
            this.frontendFlashSalePrice = frontendFlashSalePrice;
        }
    }
    
    // Getters and setters
    public List<ItemPriceValidation> getFrontendPrices() {
        return frontendPrices;
    }

    public void setFrontendPrices(List<ItemPriceValidation> frontendPrices) {
        this.frontendPrices = frontendPrices;
    }
}
