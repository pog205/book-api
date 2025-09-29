package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.CartItemResponse;
import org.datn.bookstation.entity.CartItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartItemResponseMapper {
    
    public CartItemResponse toResponse(CartItem cartItem) {
        if (cartItem == null) return null;
        
        CartItemResponse response = new CartItemResponse();
        
        // Basic info
        response.setId(cartItem.getId());
        response.setCartId(cartItem.getCart() != null ? cartItem.getCart().getId() : null);
        response.setQuantity(cartItem.getQuantity());
        response.setCreatedAt(cartItem.getCreatedAt());
        response.setUpdatedAt(cartItem.getUpdatedAt());
        response.setCreatedBy(cartItem.getCreatedBy());
        response.setUpdatedBy(cartItem.getUpdatedBy());
        response.setStatus(cartItem.getStatus());
        response.setSelected(cartItem.getSelected());
        
        // Book info
        if (cartItem.getBook() != null) {
            response.setBookId(cartItem.getBook().getId());
            response.setBookName(cartItem.getBook().getBookName());
            response.setBookCode(cartItem.getBook().getBookCode());
            
            // ✅ FIX: Lấy ảnh thường từ trường images, lấy ảnh đầu tiên
            String images = cartItem.getBook().getImages();
            if (images != null && !images.trim().isEmpty()) {
                String[] imageArray = images.split(",");
                response.setBookImageUrl(imageArray[0].trim());
            } else {
                response.setBookImageUrl(null);
            }
            
            response.setBookPrice(cartItem.getBook().getPrice());
            
            // ✅ FIX: Available stock sẽ được set ở phần flash sale/regular logic bên dưới
            // Không set ở đây để tránh override
        }
        
        // Flash sale info - LUÔN trả về flashSaleItemId nếu có liên kết
        if (cartItem.getFlashSaleItem() != null) {
            // LUÔN set flashSaleItemId bất kể status
            response.setFlashSaleItemId(cartItem.getFlashSaleItem().getId());
            
            // CHỈ hiển thị thông tin flash sale chi tiết khi status = 1 (active)
            if (cartItem.getFlashSaleItem().getStatus() == 1) {
                response.setFlashSalePrice(cartItem.getFlashSaleItem().getDiscountPrice());
                response.setFlashSaleDiscount(cartItem.getFlashSaleItem().getDiscountPercentage());
                response.setItemType("FLASH_SALE");
                response.setUnitPrice(cartItem.getFlashSaleItem().getDiscountPrice());
                response.setAvailableStock(cartItem.getFlashSaleItem().getStockQuantity());
                
                if (cartItem.getFlashSaleItem().getFlashSale() != null) {
                    response.setFlashSaleName(cartItem.getFlashSaleItem().getFlashSale().getName());
                    response.setFlashSaleEndTime(cartItem.getFlashSaleItem().getFlashSale().getEndTime());
                    
                    // Check if flash sale expired
                    long currentTime = System.currentTimeMillis();
                    response.setFlashSaleExpired(
                        cartItem.getFlashSaleItem().getFlashSale().getEndTime() < currentTime
                    );
                }
            } else {
                // FlashSale hết hạn (status = 0), hiển thị như REGULAR nhưng vẫn giữ flashSaleItemId
                response.setItemType("REGULAR");
                response.setUnitPrice(cartItem.getBook() != null ? cartItem.getBook().getPrice() : BigDecimal.ZERO);
                response.setAvailableStock(cartItem.getBook() != null ? cartItem.getBook().getStockQuantity() : 0);
            }
        } else {
            // Không có flashSaleItem, hiển thị như REGULAR
            response.setItemType("REGULAR");
            response.setUnitPrice(cartItem.getBook() != null ? cartItem.getBook().getPrice() : BigDecimal.ZERO);
            response.setAvailableStock(cartItem.getBook() != null ? cartItem.getBook().getStockQuantity() : 0);
        }
        
        // Calculate total price
        if (response.getUnitPrice() != null) {
            response.setTotalPrice(response.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        
        // Stock validation
        if (response.getAvailableStock() != null) {
            response.setOutOfStock(cartItem.getQuantity() > response.getAvailableStock());
            response.setStockLimited(response.getAvailableStock() < cartItem.getQuantity());
            response.setMaxAvailableQuantity(response.getAvailableStock());
            response.setCanAddMore(response.getAvailableStock() > cartItem.getQuantity());
            
            // Generate stock warning message
            if (response.isOutOfStock()) {
                response.setStockWarning("Hết hàng");
            } else if (response.getAvailableStock() <= 5) {
                response.setStockWarning("Chỉ còn " + response.getAvailableStock() + " sản phẩm");
            }
        }
        
        return response;
    }
}
