package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartResponse {
    private Integer id;
    private Integer userId;
    private String userEmail;
    private String userName;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private BigDecimal totalRegularAmount;
    private BigDecimal totalFlashSaleAmount; 
    private Integer regularItemsCount;
    private Integer flashSaleItemsCount;
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
    private Byte status;
    
    private List<CartItemResponse> cartItems;
    
    // Summary info
    private boolean hasOutOfStockItems;
    private boolean hasExpiredFlashSaleItems;
    private String warnings; // "Có 2 sản phẩm hết hàng, 1 flash sale đã hết hạn"
}
