package org.datn.bookstation.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.CheckoutSessionRequest;
import org.datn.bookstation.dto.response.CheckoutSessionResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.DiscountType;
import org.datn.bookstation.repository.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CheckoutSessionResponseMapper {

    private final CheckoutSessionMapper checkoutSessionMapper;
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final VoucherRepository voucherRepository;

    public CheckoutSessionResponse toResponse(CheckoutSession session) {
        CheckoutSessionResponse response = new CheckoutSessionResponse();
        
        response.setId(session.getId());
        response.setUserId(session.getUser().getId());
        
        // User info
        if (session.getUser() != null) {
            response.setUserFullName(session.getUser().getFullName());
            response.setUserEmail(session.getUser().getEmail());
        }

        // Address info
        if (session.getAddress() != null) {
            response.setAddressId(session.getAddress().getId());
            response.setAddressFullText(buildFullAddress(session.getAddress()));
            response.setRecipientName(session.getAddress().getRecipientName());
            response.setRecipientPhone(session.getAddress().getPhoneNumber());
        }

        // Shipping & payment
        response.setShippingMethod(session.getShippingMethod());
        response.setShippingFee(session.getShippingFee());
        response.setEstimatedDeliveryFrom(session.getEstimatedDeliveryFrom());
        response.setEstimatedDeliveryTo(session.getEstimatedDeliveryTo());
        response.setEstimatedDeliveryText(buildDeliveryTimeText(session.getEstimatedDeliveryFrom(), session.getEstimatedDeliveryTo()));
        response.setPaymentMethod(session.getPaymentMethod());

        // Vouchers
        List<Integer> voucherIds = checkoutSessionMapper.parseVoucherIds(session.getSelectedVoucherIds());
        response.setSelectedVoucherIds(voucherIds);
        response.setSelectedVouchers(buildVoucherSummaries(voucherIds));

        // Checkout items
        List<CheckoutSessionRequest.BookQuantity> items = checkoutSessionMapper.parseCheckoutItems(session.getCheckoutItems());
        response.setCheckoutItems(buildCheckoutItemResponses(items));

        // Financial info
        response.setSubtotal(session.getSubtotal());
        response.setTotalDiscount(session.getTotalDiscount());
        response.setTotalAmount(session.getTotalAmount());

        // Session status
        response.setStatus(session.getStatus());
        response.setExpiresAt(session.getExpiresAt());
        response.setIsExpired(session.isExpired());

        // Audit info
        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());
        response.setNotes(session.getNotes());

        return response;
    }

    private String buildFullAddress(Address address) {
        if (address == null) return null;
        
        StringBuilder sb = new StringBuilder();
        if (address.getAddressDetail() != null) sb.append(address.getAddressDetail());
        
        return sb.toString();
    }

    private String buildDeliveryTimeText(Long from, Long to) {
        if (from == null && to == null) return null;
        
        if (from != null && to != null) {
            return String.format("Từ %s đến %s", 
                java.time.Instant.ofEpochMilli(from).toString(),
                java.time.Instant.ofEpochMilli(to).toString());
        } else if (from != null) {
            return "Từ " + java.time.Instant.ofEpochMilli(from).toString();
        } else if (to != null) {
            return "Trước " + java.time.Instant.ofEpochMilli(to).toString();
        }
        return null;
    }

    private List<CheckoutSessionResponse.VoucherSummary> buildVoucherSummaries(List<Integer> voucherIds) {
        if (voucherIds == null || voucherIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<CheckoutSessionResponse.VoucherSummary> summaries = new ArrayList<>();
        
        for (Integer voucherId : voucherIds) {
            Optional<Voucher> voucherOpt = voucherRepository.findById(voucherId);
            if (voucherOpt.isPresent()) {
                Voucher voucher = voucherOpt.get();
                CheckoutSessionResponse.VoucherSummary summary = new CheckoutSessionResponse.VoucherSummary();
                
                summary.setId(voucher.getId());
                summary.setCode(voucher.getCode());
                summary.setName(voucher.getName());
                // Sử dụng VoucherCategory mới thay vì VoucherType cũ
                summary.setVoucherType(voucher.getVoucherCategory().toString());
                // Thêm kiểu giảm giá
                summary.setDiscountType(voucher.getDiscountType() != null ? voucher.getDiscountType().toString() : null);
                // Tính discount value
                BigDecimal discountValue = BigDecimal.ZERO;
                if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
                    discountValue = voucher.getDiscountPercentage() != null ? voucher.getDiscountPercentage() : BigDecimal.ZERO;
                } else if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                    discountValue = voucher.getDiscountAmount() != null ? voucher.getDiscountAmount() : BigDecimal.ZERO;
                }
                summary.setDiscountValue(discountValue);
                
                // Check validity
                long currentTime = System.currentTimeMillis();
                boolean isValid = voucher.getStatus() == 1 && 
                                currentTime >= voucher.getStartTime() && 
                                currentTime <= voucher.getEndTime();
                summary.setIsValid(isValid);
                
                if (!isValid) {
                    if (voucher.getStatus() != 1) {
                        summary.setInvalidReason("Voucher đã bị vô hiệu hóa");
                    } else if (currentTime < voucher.getStartTime()) {
                        summary.setInvalidReason("Voucher chưa có hiệu lực");
                    } else if (currentTime > voucher.getEndTime()) {
                        summary.setInvalidReason("Voucher đã hết hạn");
                    }
                }
                
                summaries.add(summary);
            }
        }
        
        return summaries;
    }

    private List<CheckoutSessionResponse.CheckoutItemResponse> buildCheckoutItemResponses(List<CheckoutSessionRequest.BookQuantity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<CheckoutSessionResponse.CheckoutItemResponse> responses = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        for (CheckoutSessionRequest.BookQuantity item : items) {
            try {
                Optional<Book> bookOpt = bookRepository.findById(item.getBookId());
                if (bookOpt.isEmpty()) {
                    log.warn("Book not found for ID: {}", item.getBookId());
                    continue;
                }
                
                Book book = bookOpt.get();
                CheckoutSessionResponse.CheckoutItemResponse response = new CheckoutSessionResponse.CheckoutItemResponse();
                
                // Basic info
                response.setBookId(item.getBookId());
                response.setQuantity(item.getQuantity());
                response.setBookTitle(book.getBookName());
                
                // ✅ FIX: Lấy ảnh từ images field (giống TrendingBookResponse) thay vì coverImageUrl
                String bookImage = null;
                if (book.getImages() != null && !book.getImages().trim().isEmpty()) {
                    // Lấy ảnh đầu tiên từ CSV string
                    String[] imageUrls = book.getImages().split(",");
                    if (imageUrls.length > 0 && !imageUrls[0].trim().isEmpty()) {
                        bookImage = imageUrls[0].trim();
                    }
                } else if (book.getCoverImageUrl() != null) {
                    // Fallback về coverImageUrl
                    bookImage = book.getCoverImageUrl();
                }
                response.setBookImage(bookImage);
                
                // Author info - get first author if available
                if (book.getAuthorBooks() != null && !book.getAuthorBooks().isEmpty()) {
                    String firstAuthor = book.getAuthorBooks().iterator().next().getAuthor().getAuthorName();
                    response.setBookAuthor(firstAuthor);
                }
                
                // Price calculation with flash sale detection - ✅ FIX: Dùng effective price
                BigDecimal originalPrice = book.getPrice(); // Giá gốc để hiển thị  
                BigDecimal effectivePrice = book.getEffectivePrice(); // Giá thực tế sau discount
                BigDecimal unitPrice = effectivePrice; // Default: dùng effective price
                boolean isFlashSale = false;
                Integer flashSaleItemId = null;
                String flashSaleName = null;
                BigDecimal savings = BigDecimal.ZERO;
                
                // Find best active flash sale
                Optional<FlashSaleItem> bestFlashSaleOpt = flashSaleItemRepository
                    .findActiveFlashSalesByBookId(item.getBookId().longValue(), currentTime)
                    .stream()
                    .filter(fs -> fs.getStockQuantity() >= item.getQuantity())
                    .findFirst();
                
                if (bestFlashSaleOpt.isPresent()) {
                    FlashSaleItem flashSaleItem = bestFlashSaleOpt.get();
                    unitPrice = flashSaleItem.getDiscountPrice();
                    isFlashSale = true;
                    flashSaleItemId = flashSaleItem.getId();
                    if (flashSaleItem.getFlashSale() != null) {
                        flashSaleName = flashSaleItem.getFlashSale().getName();
                    }
                    // Savings: so với giá gốc, không phải effective price
                    savings = originalPrice.subtract(unitPrice).multiply(BigDecimal.valueOf(item.getQuantity()));
                    
                    log.debug("Applied flash sale for book {}: regular={}, effective={}, flash={}, savings={}", 
                        item.getBookId(), originalPrice, effectivePrice, unitPrice, savings);
                } else {
                    // Không có flash sale: savings = regular price - effective price
                    if (effectivePrice.compareTo(originalPrice) < 0) {
                        savings = originalPrice.subtract(effectivePrice).multiply(BigDecimal.valueOf(item.getQuantity()));
                        log.debug("Applied book discount for book {}: regular={}, effective={}, savings={}", 
                            item.getBookId(), originalPrice, effectivePrice, savings);
                    }
                }
                
                // Set pricing info
                response.setOriginalPrice(originalPrice);
                response.setUnitPrice(unitPrice);
                response.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                response.setSavings(savings);
                
                // Flash sale info
                response.setIsFlashSale(isFlashSale);
                response.setFlashSaleItemId(flashSaleItemId);
                response.setFlashSaleName(flashSaleName);
                
                // Stock validation
                Integer availableStock = isFlashSale ? 
                    bestFlashSaleOpt.get().getStockQuantity() : 
                    book.getStockQuantity();
                    
                response.setAvailableStock(availableStock);
                response.setIsOutOfStock(availableStock < item.getQuantity());
                
                // Check if flash sale is expired (for cases where flash sale ended between creation and validation)
                if (isFlashSale && bestFlashSaleOpt.isPresent()) {
                    FlashSaleItem flashSaleItem = bestFlashSaleOpt.get();
                    boolean isExpired = flashSaleItem.getFlashSale() != null && 
                        flashSaleItem.getFlashSale().getEndTime() < currentTime;
                    response.setIsFlashSaleExpired(isExpired);
                } else {
                    response.setIsFlashSaleExpired(false);
                }
                
                responses.add(response);
                
            } catch (Exception e) {
                log.error("Error building checkout item response for book ID: {}", item.getBookId(), e);
                // Continue with other items instead of failing entirely
            }
        }
        
        return responses;
    }
}
