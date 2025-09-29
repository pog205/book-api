package org.datn.bookstation.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequest {
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    @NotNull(message = "Book ID không được để trống")
    private Integer bookId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    // Removed flashSaleItemId - Backend sẽ tự động detect flash sale tốt nhất
    
    // Optional - for batch operations
    private String notes;
}
