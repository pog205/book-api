package org.datn.bookstation.mapper;

import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.response.AuthorResponse;
import org.datn.bookstation.dto.response.TrendingBookResponse;
import org.datn.bookstation.entity.AuthorBook;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.entity.Review;
import org.datn.bookstation.entity.enums.ReviewStatus;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TrendingBookMapper {
    
    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    /**
     * Chuyển đổi từ Object[] query result sang TrendingBookResponse
     */
    public TrendingBookResponse mapToTrendingBookResponse(Object[] data, int rank, Map<Integer, List<AuthorBook>> authorsMap) {
        System.out.println("🔥 TRENDING REAL METHOD - Processing Book ID: " + data[0]);
        TrendingBookResponse response = new TrendingBookResponse();
        
        // Basic book info - mapping theo thứ tự trong query
        response.setId((Integer) data[0]); // bookId
        response.setBookName((String) data[1]); // bookName
        response.setDescription((String) data[2]); // description
        response.setPrice((BigDecimal) data[3]); // price
        response.setOriginalPrice((BigDecimal) data[3]); // originalPrice = price initially
        response.setStockQuantity((Integer) data[4]); // stockQuantity
        response.setBookCode((String) data[5]); // bookCode
        response.setPublicationDate((Long) data[6]); // publicationDate
        response.setCreatedAt((Long) data[7]); // createdAt
        response.setUpdatedAt((Long) data[8]); // updatedAt
        
        // Category info
        response.setCategoryId((Integer) data[9]); // categoryId
        response.setCategoryName((String) data[10]); // categoryName
        
        // Supplier info
        response.setSupplierId((Integer) data[11]); // supplierId
        response.setSupplierName((String) data[12]); // supplierName
        
        // Sales data - Handle possible type casting issues
        Object soldCountObj = data[13]; // soldCount
        Object orderCountObj = data[14]; // orderCount
        
        int soldCount = 0;
        if (soldCountObj != null) {
            if (soldCountObj instanceof Long) {
                soldCount = ((Long) soldCountObj).intValue();
            } else if (soldCountObj instanceof Integer) {
                soldCount = (Integer) soldCountObj;
            }
        }
        
        int orderCount = 0;
        if (orderCountObj != null) {
            if (orderCountObj instanceof Long) {
                orderCount = ((Long) orderCountObj).intValue();
            } else if (orderCountObj instanceof Integer) {
                orderCount = (Integer) orderCountObj;
            }
        }
        
        response.setSoldCount(soldCount);
        response.setOrderCount(orderCount);
        
        // ✅ FORCE OVERRIDE: Luôn lấy soldCount thực từ repository
        Integer realSoldCount = orderDetailRepository.countSoldQuantityByBook(response.getId());
        System.out.println("🔥 FORCE OVERRIDE - Book ID: " + response.getId() + " soldCount from " + soldCount + " to " + realSoldCount);
        response.setSoldCount(realSoldCount != null ? realSoldCount : 0);
        response.setOrderCount(realSoldCount != null ? realSoldCount : 0);
        
        // Review data - Handle possible type casting issues
        Double avgRating = (Double) data[15]; // avgRating
        Object reviewCountObj = data[16]; // reviewCount
        
        int reviewCount = 0;
        if (reviewCountObj != null) {
            if (reviewCountObj instanceof Long) {
                reviewCount = ((Long) reviewCountObj).intValue();
            } else if (reviewCountObj instanceof Integer) {
                reviewCount = (Integer) reviewCountObj;
            }
        }
        
        response.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        response.setReviewCount(reviewCount);
        
        // Flash sale info - Handle possible type casting issues
        Boolean isInFlashSale = (Boolean) data[17]; // isInFlashSale
        BigDecimal flashSalePrice = (BigDecimal) data[18]; // flashSalePrice
        Integer flashSaleStockQuantity = (Integer) data[19]; // flashSaleStockQuantity
        Object flashSaleSoldCountObj = data[20]; // flashSaleSoldCount
        int flashSaleSoldCount = 0;
        if (flashSaleSoldCountObj != null) {
            if (flashSaleSoldCountObj instanceof Long) {
                flashSaleSoldCount = ((Long) flashSaleSoldCountObj).intValue();
            } else if (flashSaleSoldCountObj instanceof Integer) {
                flashSaleSoldCount = (Integer) flashSaleSoldCountObj;
            }
        }
        
        response.setIsInFlashSale(isInFlashSale != null ? isInFlashSale : false);
        response.setFlashSalePrice(flashSalePrice);
        response.setFlashSaleStockQuantity(flashSaleStockQuantity);
        System.out.println("flashSaleSoldCountObj"+flashSaleSoldCount);

        response.setFlashSaleSoldCount(flashSaleSoldCount);
        
        System.out.println("🔥 TRENDING REAL - Book ID: " + response.getId() + 
                         ", isInFlashSale: " + response.getIsInFlashSale() +
                         ", flashSaleSoldCount from query: " + flashSaleSoldCount);
        
        // ✅ OVERRIDE: Lấy flashSaleSoldCount thực từ repository nếu đang trong flash sale
        if (response.getIsInFlashSale()) {
            FlashSaleItem currentFlashSale = flashSaleItemRepository.findActiveFlashSaleByBook(response.getId());
            if (currentFlashSale != null) {
                // ✅ FIX: Dùng FlashSaleItem.soldCount giống BookResponseMapper
                int realFlashSaleSoldCount = currentFlashSale.getSoldCount() != null ? currentFlashSale.getSoldCount() : 0;
                
                System.out.println("🔥 TRENDING REAL - Override flashSaleSoldCount from " + flashSaleSoldCount + 
                                 " to " + realFlashSaleSoldCount);
                response.setFlashSaleSoldCount(realFlashSaleSoldCount);
            }
        }
        
        // Calculate discount percentage and final price if in flash sale
        if (response.getIsInFlashSale() && flashSalePrice != null && response.getPrice() != null) {
            // ✅ FIX: Ensure flash sale price is valid before calculating
            if (flashSalePrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal originalPrice = response.getPrice();
                BigDecimal discount = originalPrice.subtract(flashSalePrice);
                BigDecimal discountPercentage = discount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                response.setDiscountPercentage(discountPercentage.intValue());
                // Set final price to flash sale price
                response.setPrice(flashSalePrice);
                response.setDiscountActive(true);
                log.info("✅ Applied flash sale pricing for book {}: original={}, final={}, discount={}%", 
                    response.getId(), originalPrice, flashSalePrice, discountPercentage.intValue());
            } else {
                log.warn("⚠️ Invalid flash sale price {} for book {}, using original price", 
                    flashSalePrice, response.getId());
                response.setDiscountPercentage(0);
                response.setDiscountActive(false);
            }
        } else {
            // ✅ CHECK FOR BOOK DISCOUNT if no flash sale
            // Get Book entity to check for direct discounts
            try {
                Book book = bookRepository.findById(response.getId()).orElse(null);
                if (book != null) {
                    BigDecimal effectivePrice = book.getEffectivePrice();
                    BigDecimal originalPrice = book.getPrice();
                    
                    // Check if effective price is different from original price
                    if (effectivePrice.compareTo(originalPrice) < 0) {
                        // There's a discount applied
                        BigDecimal discount = originalPrice.subtract(effectivePrice);
                        BigDecimal discountPercentage = discount.divide(originalPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        
                        response.setPrice(effectivePrice);
                        response.setDiscountPercentage(discountPercentage.intValue());
                        response.setDiscountActive(true);
                        
                        log.info("✅ Applied book discount for book {}: original={}, effective={}, discount={}%", 
                            response.getId(), originalPrice, effectivePrice, discountPercentage.intValue());
                    } else {
                        response.setDiscountPercentage(0);
                        response.setDiscountActive(false);
                        log.info("No discount for book {}, using original price: {}", 
                            response.getId(), originalPrice);
                    }
                } else {
                    log.warn("⚠️ Book entity not found for ID {}", response.getId());
                    response.setDiscountPercentage(0);
                    response.setDiscountActive(false);
                }
            } catch (Exception e) {
                log.error("❌ Error calculating book discount for ID {}: {}", response.getId(), e.getMessage());
                response.setDiscountPercentage(0);
                response.setDiscountActive(false);
            }
        }
        
        // Trending info
        response.setTrendingRank(rank);
        response.setTrendingScore(calculateTrendingScore(response));
        
        // Set authors if available
        List<AuthorBook> bookAuthors = authorsMap.get(response.getId());
        if (bookAuthors != null && !bookAuthors.isEmpty()) {
            List<AuthorResponse> authors = bookAuthors.stream()
                .map(this::mapToAuthorResponse)
                .collect(Collectors.toList());
            response.setAuthors(authors);
        }
        
        // Set images (nhiều ảnh)
        String imagesStr = (String) data[21]; // images (đã chuyển từ data[20] thành data[21])
        if (imagesStr != null && !imagesStr.isEmpty()) {
            List<String> images = java.util.Arrays.stream(imagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            response.setImages(images);
        } else {
            response.setImages(java.util.Collections.emptyList());
        }
        
        return response;
    }
    
    /**
     * Tính điểm trending dựa trên công thức business
     */
    private Double calculateTrendingScore(TrendingBookResponse book) {
        // Sales Score (40%)
        double salesScore = (book.getSoldCount() * 0.5 + book.getOrderCount() * 0.3 + 
                           (book.getOrderCount() > 0 ? 5 : 0) * 0.2) / 10.0; // Normalize to 0-10
        
        // Review Score (30%)  
        double reviewScore = (book.getRating() * 0.6 + Math.min(book.getReviewCount() / 10.0, 5) * 0.4);
        
        // Recency Score (20%) - sách mới có điểm cao hơn
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        double recencyScore = book.getCreatedAt() > thirtyDaysAgo ? 8.0 : 3.0;
        
        // Flash Sale Bonus (10%)
        double flashSaleBonus = book.getIsInFlashSale() ? 10.0 : 0.0;
        
        double totalScore = (salesScore * 0.4) + (reviewScore * 0.3) + 
                          (recencyScore * 0.2) + (flashSaleBonus * 0.1);
        
        return Math.round(totalScore * 10.0) / 10.0; // Round to 1 decimal place
    }
    
    /**
     * Chuyển đổi AuthorBook sang AuthorResponse
     */
    private AuthorResponse mapToAuthorResponse(AuthorBook authorBook) {
        AuthorResponse response = new AuthorResponse();
        response.setId(authorBook.getAuthor().getId());
        response.setAuthorName(authorBook.getAuthor().getAuthorName());
        response.setBiography(authorBook.getAuthor().getBiography());
        response.setBirthDate(authorBook.getAuthor().getBirthDate());
        response.setStatus(authorBook.getAuthor().getStatus());
        return response;
    }

    /**
     * 🔥 FALLBACK: Map fallback books (chưa có đủ dữ liệu trending)
     * Gán điểm trending thấp hơn nhưng vẫn đảm bảo có sản phẩm hiển thị
     */
    public TrendingBookResponse mapToFallbackTrendingBookResponse(Object[] data, int rank, Map<Integer, List<AuthorBook>> authorsMap) {
        System.out.println("🔥 FALLBACK METHOD - Processing Book ID: " + data[0]);
        TrendingBookResponse response = new TrendingBookResponse();
        
        // Basic book info - mapping theo thứ tự trong fallback query
        response.setId((Integer) data[0]); // bookId
        response.setBookName((String) data[1]); // bookName
        response.setDescription((String) data[2]); // description
        response.setPrice((BigDecimal) data[3]); // price
        response.setOriginalPrice((BigDecimal) data[3]); // originalPrice = price
        response.setStockQuantity((Integer) data[4]); // stockQuantity
        response.setBookCode((String) data[5]); // bookCode
        response.setPublicationDate((Long) data[6]); // publicationDate
        response.setCreatedAt((Long) data[7]); // createdAt
        response.setUpdatedAt((Long) data[8]); // updatedAt
        
        // Category info
        response.setCategoryId((Integer) data[9]); // categoryId
        response.setCategoryName((String) data[10]); // categoryName
        
        // Supplier info
        response.setSupplierId((Integer) data[11]); // supplierId
        response.setSupplierName((String) data[12]); // supplierName
        
        // ✅ FIX: Lấy số liệu thực từ repository thay vì hardcode từ query
        enrichWithRealData(response);
        
        // 🔥 FORCE OVERRIDE soldCount for Book ID 1 regardless of enrichment
        if (response.getId() == 1) {
            Integer forceSoldCount = orderDetailRepository.countSoldQuantityByBook(1);
            System.out.println("🔥🔥 FALLBACK FORCE OVERRIDE - Book ID 1 soldCount: " + forceSoldCount);
            response.setSoldCount(forceSoldCount != null ? forceSoldCount : 0);
            response.setOrderCount(response.getSoldCount());
        }
        
        // 🔥 FALLBACK TRENDING SCORE: Dựa trên độ mới và các yếu tố khác
        response.setTrendingRank(rank);
        response.setTrendingScore(calculateFallbackTrendingScore(response));
        
        // Set authors if available
        List<AuthorBook> bookAuthors = authorsMap.get(response.getId());
        if (bookAuthors != null && !bookAuthors.isEmpty()) {
            List<AuthorResponse> authors = bookAuthors.stream()
                .map(this::mapToAuthorResponse)
                .collect(Collectors.toList());
            response.setAuthors(authors);
        }
        
        // Set images (nhiều ảnh)
        String imagesStr = (String) data[13]; // images 
        if (imagesStr != null && !imagesStr.isEmpty()) {
            List<String> images = java.util.Arrays.stream(imagesStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            response.setImages(images);
        } else {
            response.setImages(java.util.Collections.emptyList());
        }
        
        return response;
    }
    
    /**
     * ✅ Lấy số liệu thực từ repository (giống BookResponseMapper)
     */
    private void enrichWithRealData(TrendingBookResponse response) {
        System.out.println("🔍 ENRICHING - Book ID: " + response.getId() + " with real data");
        
        // Sales data - Lấy từ repository giống BookResponseMapper
        Integer totalSold = orderDetailRepository.countSoldQuantityByBook(response.getId());
        response.setSoldCount(totalSold != null ? totalSold : 0);
        
        System.out.println("🔍 ENRICHING - soldCount: " + totalSold + " -> " + response.getSoldCount());
        
        // Order count - Đơn giản hóa, set bằng soldCount (vì không có method riêng)
        response.setOrderCount(response.getSoldCount());
        
        // Review data - Tính từ list reviews
        List<Review> reviews = reviewRepository.findByBookId(response.getId());
        if (reviews != null && !reviews.isEmpty()) {
            double avgRating = reviews.stream()
                    .filter(r -> r.getReviewStatus() == ReviewStatus.APPROVED)
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            int reviewCount = (int) reviews.stream()
                    .filter(r -> r.getReviewStatus() == ReviewStatus.APPROVED)
                    .count();
            
            response.setRating(Math.round(avgRating * 10.0) / 10.0);
            response.setReviewCount(reviewCount);
        } else {
            response.setRating(0.0);
            response.setReviewCount(0);
        }
        
        // Flash sale info - Lấy từ repository giống BookResponseMapper
        FlashSaleItem currentFlashSale = flashSaleItemRepository.findActiveFlashSaleByBook(response.getId());
        System.out.println("🔍 DEBUG - Book ID: " + response.getId() + ", FlashSaleItem: " + 
                          (currentFlashSale != null ? "Found (soldCount=" + currentFlashSale.getSoldCount() + ")" : "NULL"));
        
        if (currentFlashSale != null) {
            response.setIsInFlashSale(true);
            response.setFlashSalePrice(currentFlashSale.getDiscountPrice());
            response.setFlashSaleStockQuantity(currentFlashSale.getStockQuantity());
            
            // ✅ FIX: Dùng FlashSaleItem.soldCount giống BookResponseMapper
            response.setFlashSaleSoldCount(currentFlashSale.getSoldCount() != null ? currentFlashSale.getSoldCount() : 0);
            
            System.out.println("🔍 ENRICHING - FlashSaleItem.soldCount: " + currentFlashSale.getSoldCount() + 
                             ", Final flashSaleSoldCount: " + response.getFlashSaleSoldCount());
            
            // Calculate discount percentage for flash sale
            if (response.getFlashSalePrice() != null && response.getPrice() != null) {
                BigDecimal discount = response.getPrice().subtract(response.getFlashSalePrice());
                BigDecimal discountPercentage = discount.divide(response.getPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                response.setDiscountPercentage(discountPercentage.intValue());
                // Giá gốc vẫn giữ nguyên, price sẽ là giá sau discount
                response.setPrice(response.getFlashSalePrice());
                response.setDiscountActive(true);
            }
        } else {
            System.out.println("🔍 DEBUG - No active flash sale found, setting flashSaleSoldCount to 0");
            response.setIsInFlashSale(false);
            response.setFlashSalePrice(null);
            response.setFlashSaleStockQuantity(null);
            response.setFlashSaleSoldCount(0);
            response.setDiscountPercentage(0);
            response.setDiscountActive(false);
        }
    }

    /**
     * 🔥 FALLBACK: Tính trending score cho sách chưa có đủ dữ liệu trending
     * Dựa trên: Sales (30%) + Reviews (20%) + Độ mới (30%) + Flash Sale (10%) + Giá cả (10%)
     */
    private Double calculateFallbackTrendingScore(TrendingBookResponse book) {
        // Sales Score (30%) - Có data thực từ database
        double salesScore = 0.0;
        if (book.getSoldCount() > 0 || book.getOrderCount() > 0) {
            salesScore = (book.getSoldCount() * 0.6 + book.getOrderCount() * 0.4) / 5.0; // Normalize
            salesScore = Math.min(salesScore, 8.0); // Cap at 8.0
        }
        
        // Review Score (20%) - Có data thực từ database  
        double reviewScore = 0.0;
        if (book.getReviewCount() > 0) {
            reviewScore = (book.getRating() * 0.7 + Math.min(book.getReviewCount() / 5.0, 3) * 0.3);
            reviewScore = Math.min(reviewScore, 8.0); // Cap at 8.0
        }
        
        // Recency Score (30%) - Ưu tiên sách mới
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        
        double recencyScore;
        if (book.getCreatedAt() > sevenDaysAgo) {
            recencyScore = 7.0; // Sách mới trong 7 ngày
        } else if (book.getCreatedAt() > thirtyDaysAgo) {
            recencyScore = 5.0; // Sách mới trong 30 ngày
        } else {
            recencyScore = 2.0; // Sách cũ
        }
        
        // Flash Sale Bonus (10%) - Có data thực từ database
        double flashSaleBonus = 0.0;
        if (book.getIsInFlashSale()) {
            flashSaleBonus = 8.0;
            // Bonus thêm nếu có người mua flash sale
            if (book.getFlashSaleSoldCount() > 0) {
                flashSaleBonus = 10.0;
            }
        }
        
        // Price Score (10%) - Giá hợp lý có điểm cao hơn
        double priceScore = 5.0; // Điểm trung bình
        if (book.getPrice() != null) {
            // Giá từ 50k-200k: điểm cao
            // Giá < 50k hoặc > 500k: điểm thấp
            BigDecimal price = book.getPrice();
            if (price.compareTo(BigDecimal.valueOf(50000)) >= 0 && 
                price.compareTo(BigDecimal.valueOf(200000)) <= 0) {
                priceScore = 7.0;
            } else if (price.compareTo(BigDecimal.valueOf(500000)) > 0) {
                priceScore = 3.0;
            }
        }
        
        // Tổng điểm fallback
        double totalScore = (salesScore * 0.3) + (reviewScore * 0.2) + (recencyScore * 0.3) + 
                           (flashSaleBonus * 0.1) + (priceScore * 0.1);
        
        // Điểm fallback luôn < 5.0 để phân biệt với trending thực, trừ khi có sales/reviews tốt
        if (book.getSoldCount() == 0 && book.getReviewCount() == 0) {
            totalScore = Math.min(totalScore, 4.5);
        }
        
        return Math.round(totalScore * 10.0) / 10.0;
    }
}
