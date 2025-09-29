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
public class SmartCartItemRequest {
    
    @NotNull(message = "User ID không được để trống")
    private Long userId;
    
    @NotNull(message = "Book ID không được để trống")
    private Long bookId;
    
    @NotNull(message = "Quantity không được để trống")
    @Positive(message = "Quantity phải lớn hơn 0")
    private Integer quantity;
    
    // Removed preferFlashSale - Backend luôn tự động tìm flash sale tốt nhất
}
