package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.PriceValidationRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.service.PriceValidationService;
import org.datn.bookstation.service.FlashSaleService; //  THÊM FlashSaleService
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PriceValidationServiceImpl implements PriceValidationService {
    
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final FlashSaleService flashSaleService; //  THÊM FlashSaleService
    
    @Override
    public ApiResponse<String> validateProductPrices(List<PriceValidationRequest> priceValidationRequests) {
        if (priceValidationRequests == null || priceValidationRequests.isEmpty()) {
            return new ApiResponse<>(400, "Danh sách sản phẩm không được để trống", null);
        }
        
        List<String> errors = new ArrayList<>();
        
        for (PriceValidationRequest detail : priceValidationRequests) {
            String error = validateSingleProductPrice(detail.getBookId(), detail.getFrontendPrice());
            if (error != null) {
                errors.add(error);
            }
        }
        
        if (!errors.isEmpty()) {
            String errorMessage = "Giá sản phẩm đã thay đổi:\n" + String.join("\n", errors);
            return new ApiResponse<>(400, errorMessage, null);
        }
        
        return new ApiResponse<>(200, "Tất cả giá sản phẩm hợp lệ", "valid");
    }
    
    /**
     *  ENHANCED: Validate giá và số lượng flash sale
     */
    @Override
    public ApiResponse<String> validateProductPricesAndQuantities(List<PriceValidationRequest> priceValidationRequests, Integer userId) {
        if (priceValidationRequests == null || priceValidationRequests.isEmpty()) {
            return new ApiResponse<>(400, "Danh sách sản phẩm không được để trống", null);
        }
        
        if (userId == null) {
            return new ApiResponse<>(400, "User ID không được để trống", null);
        }
        
        List<String> errors = new ArrayList<>();
        
        for (PriceValidationRequest detail : priceValidationRequests) {
            //  VALIDATE SỐ LƯỢNG TRƯỚC, sau đó mới validate giá
            String error = validateSingleProductPriceAndQuantity(
                detail.getBookId(), 
                detail.getFrontendPrice(), 
                detail.getQuantity(), 
                userId
            );
            if (error != null) {
                errors.add(error);
            }
        }
        
        if (!errors.isEmpty()) {
            String errorMessage = String.join("\n", errors);
            return new ApiResponse<>(400, errorMessage, null);
        }
        
        return new ApiResponse<>(200, "Tất cả sản phẩm hợp lệ", "valid");
    }
    
    @Override
    public String validateSingleProductPrice(Integer bookId, BigDecimal frontendPrice) {
        return validateSingleProductPriceAndQuantity(bookId, frontendPrice, null, null);
    }
    
    /**
     *  ENHANCED: Validate một sản phẩm với số lượng và flash sale limit
     */
    @Override
    public String validateSingleProductPriceAndQuantity(Integer bookId, BigDecimal frontendPrice, Integer quantity, Integer userId) {
        if (bookId == null) {
            return "Book ID không hợp lệ";
        }
        
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return "Không tìm thấy sách với ID: " + bookId;
        }
        
        if (frontendPrice == null) {
            return "Giá frontend không được để trống";
        }
        
        //  BƯỚC 1: VALIDATE SỐ LƯỢNG FLASH SALE TRƯỚC (ưu tiên)
        if (quantity != null && userId != null) {
            FlashSaleItem activeFlashSale = getCurrentActiveFlashSale(bookId);
            if (activeFlashSale != null) {
                // 1.1. Validate stock flash sale
                if (activeFlashSale.getStockQuantity() < quantity) {
                    return String.format("Flash sale sách '%s' chỉ còn %d sản phẩm, không đủ cho yêu cầu %d sản phẩm", 
                        book.getBookName(), activeFlashSale.getStockQuantity(), quantity);
                }
                
                // 1.2.  ENHANCED: Validate giới hạn mua per user với hai loại thông báo khác nhau
                if (!flashSaleService.canUserPurchaseMore(activeFlashSale.getId().longValue(), userId, quantity)) {
                    int currentPurchased = flashSaleService.getUserPurchasedQuantity(activeFlashSale.getId().longValue(), userId);
                    int maxAllowed = activeFlashSale.getMaxPurchasePerUser();
                    
                    //  LOẠI 1: Đã đạt giới hạn tối đa, không thể mua nữa
                    if (currentPurchased >= maxAllowed) {
                        return String.format("Bạn đã mua đủ %d sản phẩm flash sale '%s' cho phép. Không thể mua thêm.", 
                            maxAllowed, book.getBookName());
                    }
                    
                    //  LOẠI 2: Chưa đạt giới hạn nhưng đặt quá số lượng cho phép
                    int remainingAllowed = maxAllowed - currentPurchased;
                    if (quantity > remainingAllowed) {
                        return String.format("Bạn đã mua %d sản phẩm, chỉ được mua thêm tối đa %d sản phẩm flash sale '%s'.", 
                            currentPurchased, remainingAllowed, book.getBookName());
                    }
                    
                    //  LOẠI 3: Thông báo chung cho trường hợp đặc biệt khác
                    return String.format("Bạn chỉ được mua tối đa %d sản phẩm flash sale '%s'.", 
                        maxAllowed, book.getBookName());
                }
            }
        }
        
        //  BƯỚC 2: VALIDATE GIÁ (sau khi số lượng hợp lệ)
        BigDecimal currentBookPrice = getCurrentBookPrice(book);
        BigDecimal currentFlashSalePrice = null;
        FlashSaleItem activeFlashSale = getCurrentActiveFlashSale(bookId);
        
        if (activeFlashSale != null) {
            currentFlashSalePrice = activeFlashSale.getDiscountPrice();            
        }
        
        BigDecimal expectedPrice = currentFlashSalePrice != null ? currentFlashSalePrice : currentBookPrice;
        if (frontendPrice.compareTo(expectedPrice) != 0) {
            return String.format("Giá của sách '%s' đã thay đổi từ %s VND thành %s VND", 
                                book.getBookName(), 
                                formatPrice(frontendPrice),
                                formatPrice(expectedPrice));
        }
        
        return null; // Hợp lệ
    }
    
    /**
     * Lấy giá hiện tại của sách (đã bao gồm discount nếu có)
     */
    private BigDecimal getCurrentBookPrice(Book book) {
        BigDecimal basePrice = book.getPrice();
        
        // Áp dụng discount nếu có và đang active
        if (book.getDiscountActive() != null && book.getDiscountActive()) {
            if (book.getDiscountValue() != null && book.getDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
                basePrice = basePrice.subtract(book.getDiscountValue());
            } else if (book.getDiscountPercent() != null && book.getDiscountPercent() > 0) {
                BigDecimal discountAmount = basePrice.multiply(new BigDecimal(book.getDiscountPercent()))
                                                    .divide(new BigDecimal("100"));
                basePrice = basePrice.subtract(discountAmount);
            }
        }
        
        // Đảm bảo giá không âm
        if (basePrice.compareTo(BigDecimal.ZERO) < 0) {
            basePrice = BigDecimal.ZERO;
        }
        
        return basePrice;
    }
    
    /**
     * Lấy flash sale đang hoạt động cho sách
     */
    private FlashSaleItem getCurrentActiveFlashSale(Integer bookId) {
        return flashSaleItemRepository.findAll().stream()
            .filter(item -> item.getBook() != null && item.getBook().getId().equals(bookId))
            .filter(item -> item.getStatus() != null && item.getStatus() == 1)
            .filter(item -> item.getFlashSale() != null && item.getFlashSale().getStatus() != null && item.getFlashSale().getStatus() == 1)
            .filter(item -> {
                // Kiểm tra thời gian hiệu lực
                long now = System.currentTimeMillis();
                return item.getFlashSale().getStartTime() != null && item.getFlashSale().getStartTime() <= now &&
                       item.getFlashSale().getEndTime() != null && item.getFlashSale().getEndTime() > now;
            })
            .filter(item -> {
                // Kiểm tra số lượng còn lại
                int quantityRemaining = item.getStockQuantity() - 
                                       (item.getSoldCount() != null ? item.getSoldCount() : 0);
                return quantityRemaining > 0;
            })
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Format giá tiền để hiển thị
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) return "0";
        return String.format("%,.0f", price);
    }
}
