package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.AuthorResponse;
import org.datn.bookstation.dto.response.BookDetailResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookDetailResponseMapper {
    
    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;
    
    public BookDetailResponse toDetailResponse(Book book) {
        if (book == null) return null;
        
        BookDetailResponse response = new BookDetailResponse();
        
        // Basic info
        response.setId(book.getId());
        response.setBookName(book.getBookName());
        response.setDescription(book.getDescription());
        response.setPrice(book.getPrice());
        response.setStockQuantity(book.getStockQuantity());
        response.setPublicationDate(book.getPublicationDate());
        response.setBookCode(book.getBookCode());
        response.setStatus(book.getStatus());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());
        
        // Detail fields
        response.setCoverImageUrl(book.getCoverImageUrl());
        response.setTranslator(book.getTranslator());
        response.setIsbn(book.getIsbn());
        response.setPageCount(book.getPageCount());
        response.setLanguage(book.getLanguage());
        response.setWeight(book.getWeight());
        response.setDimensions(book.getDimensions());
        
        // Category info
        if (book.getCategory() != null) {
            response.setCategoryId(book.getCategory().getId());
            response.setCategoryName(book.getCategory().getCategoryName());
        }
        
        // Supplier info
        if (book.getSupplier() != null) {
            response.setSupplierId(book.getSupplier().getId());
            response.setSupplierName(book.getSupplier().getSupplierName());
        }
        
        // Publisher info
        if (book.getPublisher() != null) {
            response.setPublisherId(book.getPublisher().getId());
            response.setPublisherName(book.getPublisher().getPublisherName());
        }
        
        // Authors info
        if (book.getAuthorBooks() != null && !book.getAuthorBooks().isEmpty()) {
            List<AuthorResponse> authors = book.getAuthorBooks().stream()
                .map(authorBook -> {
                    AuthorResponse authorResponse = new AuthorResponse();
                    authorResponse.setId(authorBook.getAuthor().getId());
                    authorResponse.setAuthorName(authorBook.getAuthor().getAuthorName());
                    authorResponse.setBiography(authorBook.getAuthor().getBiography());
                    authorResponse.setBirthDate(authorBook.getAuthor().getBirthDate());
                    authorResponse.setStatus(authorBook.getAuthor().getStatus());
                    return authorResponse;
                })
                .collect(Collectors.toList());
            response.setAuthors(authors);
        }
        
        // ✅ THÊM MỚI: Set images (giống TrendingBookMapper)
        if (book.getImages() != null && !book.getImages().isEmpty()) {
            List<String> images = java.util.Arrays.stream(book.getImages().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            response.setImages(images);
        } else {
            response.setImages(java.util.Collections.emptyList());
        }
        
        // 🔥 Xử lý giá và discount (Flash Sale → Direct Discount → Original Price)
        setPriceAndDiscountFields(response, book);
        
        // 🔥 Luôn set server time (chống hack client time)
        response.setServerTime(System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * Xử lý logic giá và discount: Flash Sale → Direct Discount → Original Price
     */
    private void setPriceAndDiscountFields(BookDetailResponse response, Book book) {
        // Step 1: Check Flash Sale đang active
        long currentTime = System.currentTimeMillis();
        List<FlashSaleItem> activeFlashSales = flashSaleItemRepository
            .findCurrentActiveFlashSaleByBookId(book.getId(), currentTime);
        
        if (!activeFlashSales.isEmpty()) {
            // ⭐ CASE 1: Flash Sale active (ưu tiên cao nhất)
            FlashSaleItem flashSaleItem = activeFlashSales.get(0);
            
            // Override giá chính = giá flash sale
            response.setPrice(flashSaleItem.getDiscountPrice());
            
            // Set thông tin flash sale cho frontend
            response.setFlashSalePrice(flashSaleItem.getDiscountPrice());
            response.setFlashSaleStock(flashSaleItem.getStockQuantity()); // ✅ THÊM: Số lượng flash sale còn lại
            response.setFlashSaleDiscount(flashSaleItem.getDiscountPercentage());
            response.setFlashSaleEndTime(flashSaleItem.getFlashSale().getEndTime());
            
            // ✅ FIX: Dùng trực tiếp field soldCount thay vì query phức tạp
            response.setFlashSaleSoldCount(flashSaleItem.getSoldCount() != null ? flashSaleItem.getSoldCount() : 0);
            
            // Override stock quantity = flash sale stock
            response.setStockQuantity(flashSaleItem.getStockQuantity());
            
            // Clear direct discount vì flash sale ưu tiên hơn
            response.setDiscountValue(null);
            response.setDiscountPercent(null);
            
            return;
        }
        
        // Step 2: Check Direct Discount (không có flash sale)
        if (book.getDiscountActive() != null && book.getDiscountActive() && 
            (book.getDiscountValue() != null || book.getDiscountPercent() != null)) {
            
            // ⭐ CASE 2: Direct Discount active
            java.math.BigDecimal originalPrice = book.getPrice();
            java.math.BigDecimal discountedPrice = originalPrice;
            
            // Tính giá sau khi giảm
            if (book.getDiscountValue() != null) {
                // Giảm theo giá trị cố định
                discountedPrice = originalPrice.subtract(book.getDiscountValue());
            } else if (book.getDiscountPercent() != null) {
                // Giảm theo phần trăm
                java.math.BigDecimal discountAmount = originalPrice
                    .multiply(java.math.BigDecimal.valueOf(book.getDiscountPercent()))
                    .divide(java.math.BigDecimal.valueOf(100));
                discountedPrice = originalPrice.subtract(discountAmount);
            }
            
            // Đảm bảo giá không âm
            if (discountedPrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
                discountedPrice = java.math.BigDecimal.ZERO;
            }
            
            // Override giá chính = giá sau discount
            response.setPrice(discountedPrice);
            
            // Set thông tin discount cho frontend
            response.setDiscountValue(book.getDiscountValue());
            response.setDiscountPercent(book.getDiscountPercent());
            
            // Clear flash sale vì không có
            response.setFlashSalePrice(null);
            response.setFlashSaleStock(null); // ✅ THÊM
            response.setFlashSaleDiscount(null);
            response.setFlashSaleEndTime(null);
            response.setFlashSaleSoldCount(null);
            
            return;
        }
        
        // ⭐ CASE 3: Không có flash sale, không có discount → giá gốc
        // Giá và stock giữ nguyên từ book gốc
        response.setFlashSalePrice(null);
        response.setFlashSaleStock(null); // ✅ THÊM
        response.setFlashSaleDiscount(null);
        response.setFlashSaleEndTime(null);
        response.setFlashSaleSoldCount(null);
        response.setDiscountValue(null);
        response.setDiscountPercent(null);
    }


}
