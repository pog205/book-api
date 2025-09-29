package org.datn.bookstation.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Data
public class OrderCalculationRequest {
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    @NotNull(message = "Phí vận chuyển không được để trống")
    @Positive(message = "Phí vận chuyển phải lớn hơn 0")
    private BigDecimal shippingFee;
    
    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    private List<OrderItemCalculationRequest> items;
    
    private List<Integer> voucherIds; // Optional vouchers
    
    @Getter
    @Setter
    @Data
    public static class OrderItemCalculationRequest {
        @NotNull(message = "Book ID không được để trống")
        private Integer bookId;
        
        @NotNull(message = "Số lượng không được để trống")
        @Positive(message = "Số lượng phải lớn hơn 0")
        private Integer quantity;
    }
}
