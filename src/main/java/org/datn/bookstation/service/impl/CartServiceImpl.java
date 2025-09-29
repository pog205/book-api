package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CartResponse;
import org.datn.bookstation.dto.response.CartSummaryResponse;
import org.datn.bookstation.dto.response.CartItemResponse;
import org.datn.bookstation.entity.Cart;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.mapper.CartResponseMapper;
import org.datn.bookstation.repository.CartRepository;
import org.datn.bookstation.repository.CartItemRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.CartService;
import org.datn.bookstation.service.CartItemService;
import org.datn.bookstation.service.FlashSaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final CartResponseMapper cartResponseMapper;
    private final CartItemService cartItemService;
    private final FlashSaleService flashSaleService;

    @Override
    public CartResponse getCartByUserId(Integer userId) {
        Cart cart = getOrCreateCartEntity(userId);
        CartResponse response = cartResponseMapper.toResponse(cart);
        
        if (response != null) {
            // Lấy cart items
            List<CartItemResponse> cartItems = cartItemService.getCartItemsByUserId(userId);
            response.setCartItems(cartItems);
            
            // Tính toán thống kê
            calculateCartStatistics(response, cartItems);
        }
        
        return response;
    }

    @Override
    public CartSummaryResponse getCartSummary(Integer userId) {
        Cart cart = getOrCreateCartEntity(userId);
        CartSummaryResponse response = cartResponseMapper.toSummaryResponse(cart);
        
        if (response != null) {
            List<CartItemResponse> cartItems = cartItemService.getCartItemsByUserId(userId);
            calculateSummaryStatistics(response, cartItems);
        }
        
        return response;
    }

    @Override
    public ApiResponse<Cart> createCart(Integer userId) {
        try {
            // Kiểm tra user tồn tại
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return new ApiResponse<>(404, "User không tồn tại", null);
            }
            
            // Kiểm tra đã có cart chưa
            Optional<Cart> existingCart = cartRepository.findByUserId(userId);
            if (existingCart.isPresent()) {
                return new ApiResponse<>(200, "Giỏ hàng đã tồn tại", existingCart.get());
            }
            
            // Tạo cart mới
            Cart cart = new Cart();
            cart.setUser(userOpt.get());
            cart.setCreatedBy(userId);
            cart.setStatus((byte) 1);
            
            Cart savedCart = cartRepository.save(cart);
            return new ApiResponse<>(201, "Tạo giỏ hàng thành công", savedCart);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi tạo giỏ hàng: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<String> clearCart(Integer userId) {
        try {
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy giỏ hàng", null);
            }
            
            // Xóa tất cả cart items
            cartItemRepository.deleteByCartId(cartOpt.get().getId());
            
            return new ApiResponse<>(200, "Xóa toàn bộ giỏ hàng thành công", "OK");
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi xóa giỏ hàng: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<CartResponse> validateAndUpdateCart(Integer userId) {
        try {
            // Validate và update cart items
            cartItemService.validateAndUpdateCartItems(userId);
            
            // Lấy cart response mới nhất
            CartResponse cartResponse = getCartByUserId(userId);
            
            return new ApiResponse<>(200, "Cập nhật giỏ hàng thành công", cartResponse);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi validate giỏ hàng: " + e.getMessage(), null);
        }
    }

    @Override
    public Cart getOrCreateCartEntity(Integer userId) {
        Optional<Cart> cartOpt = cartRepository.findActiveCartByUserId(userId);
        
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        }
        
        // Tạo cart mới nếu chưa có
        ApiResponse<Cart> createResponse = createCart(userId);
        if (createResponse.getStatus() == 201 || createResponse.getStatus() == 200) {
            return createResponse.getData();
        }
        
        throw new RuntimeException("Không thể tạo giỏ hàng cho user: " + userId);
    }

    @Override
    public ApiResponse<Cart> toggleCartStatus(Integer cartId) {
        try {
            Optional<Cart> cartOpt = cartRepository.findById(cartId);
            if (cartOpt.isEmpty()) {
                return new ApiResponse<>(404, "Không tìm thấy giỏ hàng", null);
            }
            
            Cart cart = cartOpt.get();
            cart.setStatus(cart.getStatus() == 1 ? (byte) 0 : (byte) 1);
            
            Cart savedCart = cartRepository.save(cart);
            return new ApiResponse<>(200, "Cập nhật trạng thái giỏ hàng thành công", savedCart);
            
        } catch (Exception e) {
            return new ApiResponse<>(500, "Lỗi khi cập nhật trạng thái: " + e.getMessage(), null);
        }
    }
    
    private void calculateCartStatistics(CartResponse response, List<CartItemResponse> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            response.setTotalItems(0);
            response.setTotalAmount(BigDecimal.ZERO);
            response.setTotalRegularAmount(BigDecimal.ZERO);
            response.setTotalFlashSaleAmount(BigDecimal.ZERO);
            response.setRegularItemsCount(0);
            response.setFlashSaleItemsCount(0);
            response.setHasOutOfStockItems(false);
            response.setHasExpiredFlashSaleItems(false);
            return;
        }
        
        response.setTotalItems(cartItems.size());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalRegularAmount = BigDecimal.ZERO;
        BigDecimal totalFlashSaleAmount = BigDecimal.ZERO;
        int regularCount = 0;
        int flashSaleCount = 0;
        boolean hasOutOfStock = false;
        boolean hasExpiredFlashSale = false;
        
        for (CartItemResponse item : cartItems) {
            if (item.getTotalPrice() != null) {
                totalAmount = totalAmount.add(item.getTotalPrice());
                
                if ("FLASH_SALE".equals(item.getItemType())) {
                    totalFlashSaleAmount = totalFlashSaleAmount.add(item.getTotalPrice());
                    flashSaleCount++;
                    if (item.isFlashSaleExpired()) {
                        hasExpiredFlashSale = true;
                    }
                } else {
                    totalRegularAmount = totalRegularAmount.add(item.getTotalPrice());
                    regularCount++;
                }
            }
            
            if (item.isOutOfStock()) {
                hasOutOfStock = true;
            }
        }
        
        response.setTotalAmount(totalAmount);
        response.setTotalRegularAmount(totalRegularAmount);
        response.setTotalFlashSaleAmount(totalFlashSaleAmount);
        response.setRegularItemsCount(regularCount);
        response.setFlashSaleItemsCount(flashSaleCount);
        response.setHasOutOfStockItems(hasOutOfStock);
        response.setHasExpiredFlashSaleItems(hasExpiredFlashSale);
        
        // Generate warnings
        StringBuilder warnings = new StringBuilder();
        if (hasOutOfStock) {
            warnings.append("Có sản phẩm hết hàng. ");
        }
        if (hasExpiredFlashSale) {
            warnings.append("Có flash sale đã hết hạn. ");
        }
        response.setWarnings(warnings.toString().trim());
    }
    
    private void calculateSummaryStatistics(CartSummaryResponse response, List<CartItemResponse> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            response.setHasItems(false);
            response.setTotalItems(0);
            response.setTotalQuantity(0);
            response.setTotalAmount(BigDecimal.ZERO);
            response.setTotalRegularAmount(BigDecimal.ZERO);
            response.setTotalFlashSaleAmount(BigDecimal.ZERO);
            response.setTotalSavings(BigDecimal.ZERO);
            response.setRegularItemsCount(0);
            response.setFlashSaleItemsCount(0);
            response.setReadyForCheckout(false);
            response.setStatusMessage("Giỏ hàng trống");
            return;
        }
        
        response.setHasItems(true);
        response.setTotalItems(cartItems.size());
        
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalRegularAmount = BigDecimal.ZERO;
        BigDecimal totalFlashSaleAmount = BigDecimal.ZERO;
        BigDecimal totalSavings = BigDecimal.ZERO;
        int regularCount = 0;
        int flashSaleCount = 0;
        boolean hasOutOfStock = false;
        boolean hasExpiredFlashSale = false;
        
        for (CartItemResponse item : cartItems) {
            totalQuantity += item.getQuantity();
            
            if (item.getTotalPrice() != null) {
                totalAmount = totalAmount.add(item.getTotalPrice());
                
                if ("FLASH_SALE".equals(item.getItemType())) {
                    totalFlashSaleAmount = totalFlashSaleAmount.add(item.getTotalPrice());
                    flashSaleCount++;
                    
                    // Tính tiền tiết kiệm
                    if (item.getBookPrice() != null && item.getFlashSalePrice() != null) {
                        BigDecimal savings = item.getBookPrice().subtract(item.getFlashSalePrice())
                                           .multiply(BigDecimal.valueOf(item.getQuantity()));
                        totalSavings = totalSavings.add(savings);
                    }
                    
                    if (item.isFlashSaleExpired()) {
                        hasExpiredFlashSale = true;
                    }
                } else {
                    totalRegularAmount = totalRegularAmount.add(item.getTotalPrice());
                    regularCount++;
                }
            }
            
            if (item.isOutOfStock()) {
                hasOutOfStock = true;
            }
        }
        
        response.setTotalQuantity(totalQuantity);
        response.setTotalAmount(totalAmount);
        response.setTotalRegularAmount(totalRegularAmount);
        response.setTotalFlashSaleAmount(totalFlashSaleAmount);
        response.setTotalSavings(totalSavings);
        response.setRegularItemsCount(regularCount);
        response.setFlashSaleItemsCount(flashSaleCount);
        response.setHasOutOfStockItems(hasOutOfStock);
        response.setHasExpiredFlashSaleItems(hasExpiredFlashSale);
        
        // Determine status
        boolean readyForCheckout = !hasOutOfStock && !hasExpiredFlashSale;
        response.setReadyForCheckout(readyForCheckout);
        
        if (readyForCheckout) {
            response.setStatusMessage("Sẵn sàng thanh toán");
        } else {
            StringBuilder warnings = new StringBuilder();
            if (hasOutOfStock) {
                warnings.append("Có sản phẩm hết hàng. ");
            }
            if (hasExpiredFlashSale) {
                warnings.append("Có flash sale đã hết hạn. ");
            }
            response.setWarningMessage(warnings.toString().trim());
            response.setStatusMessage("Cần xem xét giỏ hàng");
        }
    }
    
    @Override
    public Integer getCartItemsCount(Integer userId) {
        try {
            // Sử dụng method tối ưu từ CartRepository
            Integer count = cartRepository.countItemsByUserId(userId);
            return count != null ? count : 0;
            
        } catch (Exception e) {
            // Log error nếu cần
            return 0;
        }
    }
}
