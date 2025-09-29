package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CartResponse;
import org.datn.bookstation.dto.response.CartSummaryResponse;
import org.datn.bookstation.entity.Cart;



public interface CartService {
    
    /**
     * Lấy giỏ hàng của user (tạo mới nếu chưa có)
     */
    CartResponse getCartByUserId(Integer userId);
    
    /**
     * Lấy tóm tắt giỏ hàng (không bao gồm chi tiết items)
     */
    CartSummaryResponse getCartSummary(Integer userId);
    
    /**
     * Tạo giỏ hàng mới cho user
     */
    ApiResponse<Cart> createCart(Integer userId);
    
    /**
     * Xóa toàn bộ items trong giỏ hàng
     */
    ApiResponse<String> clearCart(Integer userId);
    
    /**
     * Kiểm tra và cập nhật trạng thái giỏ hàng
     * (xử lý flash sale hết hạn, sách hết hàng)
     */
    ApiResponse<CartResponse> validateAndUpdateCart(Integer userId);
    
    /**
     * Lấy entity Cart cho internal use
     */
    Cart getOrCreateCartEntity(Integer userId);
    
    /**
     * Toggle trạng thái giỏ hàng
     */
    ApiResponse<Cart> toggleCartStatus(Integer cartId);
    
    /**
     * Đếm số lượng sách trong giỏ hàng của user
     */
    Integer getCartItemsCount(Integer userId);
}
