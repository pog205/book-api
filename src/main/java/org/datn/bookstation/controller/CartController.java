package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CartResponse;
import org.datn.bookstation.dto.response.CartSummaryResponse;
import org.datn.bookstation.entity.Cart;
import org.datn.bookstation.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    
    private final CartService cartService;

    /**
     * Lấy giỏ hàng đầy đủ của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCartByUserId(@PathVariable Integer userId) {
        try {
            CartResponse cart = cartService.getCartByUserId(userId);
            if (cart == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Không tìm thấy giỏ hàng", null));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy giỏ hàng thành công", cart));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Lỗi khi lấy giỏ hàng: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy tóm tắt giỏ hàng (không có chi tiết items)
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getCartSummary(@PathVariable Integer userId) {
        try {
            CartSummaryResponse summary = cartService.getCartSummary(userId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy tóm tắt giỏ hàng thành công", summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Lỗi khi lấy tóm tắt: " + e.getMessage(), null));
        }
    }
     
    /**
     * Đếm số lượng sách trong giỏ hàng của user
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemsCount(@PathVariable Integer userId) {
        try {
            Integer count = cartService.getCartItemsCount(userId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Đếm số lượng sách thành công", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Lỗi khi đếm số lượng sách: " + e.getMessage(), null));
        }
    }

    /**
     * Tạo giỏ hàng mới cho user
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Cart>> createCart(@PathVariable Integer userId) {
        ApiResponse<Cart> response = cartService.createCart(userId);
        
        if (response.getStatus() == 201) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (response.getStatus() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Integer userId) {
        ApiResponse<String> response = cartService.clearCart(userId);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }

    /**
     * Validate và cập nhật giỏ hàng
     * (xử lý flash sale hết hạn, sách hết hàng)
     */
    @PostMapping("/user/{userId}/validate")
    public ResponseEntity<ApiResponse<CartResponse>> validateAndUpdateCart(@PathVariable Integer userId) {
        ApiResponse<CartResponse> response = cartService.validateAndUpdateCart(userId);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }

    /**
     * Toggle trạng thái giỏ hàng (Admin)
     */
    @PatchMapping("/{cartId}/toggle-status")
    public ResponseEntity<ApiResponse<Cart>> toggleCartStatus(@PathVariable Integer cartId) {
        ApiResponse<Cart> response = cartService.toggleCartStatus(cartId);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }
}
