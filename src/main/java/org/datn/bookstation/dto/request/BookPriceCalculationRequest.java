package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BookPriceCalculationRequest {
    @NotNull(message = "Book ID không được để trống")
    private Integer bookId;
    
    // Discount theo số tiền
    @DecimalMin(value = "0", message = "Discount value phải >= 0")
    private BigDecimal discountValue;
    
    // Discount theo phần trăm  
    @DecimalMin(value = "0", message = "Discount percent phải >= 0")
    private Integer discountPercent;
    
    // Có áp dụng discount hay không
    private Boolean discountActive = false;
}
