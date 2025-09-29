package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.CartItemRequest;
import org.datn.bookstation.dto.request.BatchCartItemRequest;
import org.datn.bookstation.dto.request.SmartCartItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CartItemResponse;
import org.datn.bookstation.service.CartItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/carts/items")
public class CartItemController {
    
    private final CartItemService cartItemService;

    /**
     * Lấy tất cả items trong giỏ hàng của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCartItemsByUserId(@PathVariable Integer userId) {
        try {
            List<CartItemResponse> cartItems = cartItemService.getCartItemsByUserId(userId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách sản phẩm thành công", cartItems));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Lỗi khi lấy danh sách: " + e.getMessage(), null));
        }
    }

    /**
     * Thêm sản phẩm vào giỏ hàng (Auto-detect flash sale)
     * Backend sẽ tự động tìm flash sale tốt nhất cho sản phẩm
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addItemToCart(@Valid @RequestBody CartItemRequest request) {
        ApiResponse<CartItemResponse> response = cartItemService.addItemToCart(request);
        
        if (response.getStatus() == 200) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
        }
    }

    /**
     * Cập nhật số lượng CartItem
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable Integer cartItemId, 
            @RequestParam Integer quantity) {
        
        ApiResponse<CartItemResponse> response = cartItemService.updateCartItem(cartItemId, quantity);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }

    /**
     * Xóa CartItem khỏi giỏ hàng
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<String>> removeCartItem(@PathVariable Integer cartItemId) {
        ApiResponse<String> response = cartItemService.removeCartItem(cartItemId);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }

    /**
     * Thêm nhiều sản phẩm cùng lúc (batch)
     */
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> addItemsToCartBatch(
            @Valid @RequestBody BatchCartItemRequest request) {
        
        ApiResponse<List<CartItemResponse>> response = cartItemService.addItemsToCartBatch(request);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }

    /**
     * Validate và cập nhật các CartItems của user
     */
    @PostMapping("/user/{userId}/validate")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> validateAndUpdateCartItems(@PathVariable Integer userId) {
        ApiResponse<List<CartItemResponse>> response = cartItemService.validateAndUpdateCartItems(userId);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }

    /**
     * Kiểm tra ownership của CartItem
     */
    @GetMapping("/{cartItemId}/check-ownership/{userId}")
    public ResponseEntity<ApiResponse<Boolean>> checkCartItemOwnership(
            @PathVariable Integer cartItemId, 
            @PathVariable Integer userId) {
        
        try {
            boolean belongs = cartItemService.isCartItemBelongsToUser(cartItemId, userId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Kiểm tra quyền sở hữu thành công", belongs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Lỗi khi kiểm tra: " + e.getMessage(), null));
        }
    }

    /**
     * 🔄 NEW: Sync cart items khi flash sale được gia hạn (Admin only)
     */
    @PostMapping("/sync-flash-sale/{flashSaleId}")
    public ResponseEntity<ApiResponse<Integer>> syncCartItemsWithFlashSale(@PathVariable Integer flashSaleId) {
        try {
            int syncedCount = cartItemService.syncCartItemsWithUpdatedFlashSale(flashSaleId);
            return ResponseEntity.ok(new ApiResponse<>(200, 
                "Đã sync " + syncedCount + " cart items với flash sale " + flashSaleId, syncedCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Lỗi khi sync: " + e.getMessage(), null));
        }
    }

    /**
     * 🧹 NEW: Merge duplicate cart items cho user (Admin only)
     */
    @PostMapping("/merge-duplicates/{userId}")
    public ResponseEntity<ApiResponse<Integer>> mergeDuplicateCartItems(@PathVariable Integer userId) {
        try {
            int mergedCount = cartItemService.mergeDuplicateCartItemsForUser(userId);
            return ResponseEntity.ok(new ApiResponse<>(200, 
                "Đã merge " + mergedCount + " duplicate cart items cho user " + userId, mergedCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Lỗi khi merge: " + e.getMessage(), null));
        }
    }

    /**
     * Thêm sản phẩm vào giỏ hàng thông minh (Deprecated - sử dụng POST /api/carts/items)
     * Endpoint này giữ lại để backward compatibility
     */
    @PostMapping("/smart")
    @Deprecated
    public ResponseEntity<ApiResponse<CartItemResponse>> addSmartItemToCart(
            @RequestBody @Valid SmartCartItemRequest request) {
        try {
            ApiResponse<CartItemResponse> response = cartItemService.addSmartItemToCart(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        }
    }

    /**
     * Cập nhật trạng thái chọn/bỏ CartItem (toggle)
     */
    @PutMapping("/{cartItemId}/select")
    public ResponseEntity<ApiResponse<CartItemResponse>> toggleCartItemSelected(
            @PathVariable Integer cartItemId) {
        ApiResponse<CartItemResponse> response = cartItemService.toggleCartItemSelected(cartItemId);
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatus())).body(response);
    }
}
