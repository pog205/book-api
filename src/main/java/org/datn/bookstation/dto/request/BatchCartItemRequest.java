package org.datn.bookstation.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

@Data
public class BatchCartItemRequest {
    
    @NotNull(message = "User ID không được để trống")
    private Integer userId;
    
    @NotNull(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<CartItemRequest> items;
    
    private String notes;
}
