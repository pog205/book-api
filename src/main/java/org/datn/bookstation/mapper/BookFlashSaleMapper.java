package org.datn.bookstation.mapper;

import lombok.AllArgsConstructor;
import org.datn.bookstation.dto.request.FlashSaleItemBookRequest;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class BookFlashSaleMapper {

    private final FlashSaleItemRepository flashSaleItemRepository;

    public static FlashSaleItemBookRequest mapToFlashSaleItemBookRequest(Object[] data,
            FlashSaleItemRepository flashSaleItemRepository,
            OrderDetailRepository orderDetailRepository) { // Thêm parameter
        FlashSaleItemBookRequest dto = new FlashSaleItemBookRequest();
        BigDecimal flashSalePrice = (BigDecimal) data[7];
        dto.setId(data[0] != null ? ((Number) data[0]).intValue() : null); // id
        dto.setBookName((String) data[1]); // bookName
        dto.setPrice(data[2] != null ? (BigDecimal) data[2] : null); // price
        dto.setOriginalPrice(data[2] != null ? (BigDecimal) data[2] : null); // originalPrice = price
        dto.setStockQuantity(data[3] != null ? ((Number) data[3]).intValue() : null); // stockQuantity

        // images: String, split thành List<String>
        String imagesStr = (String) data[4];
        if (imagesStr != null && !imagesStr.isEmpty()) {
            List<String> images = Arrays.stream(imagesStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            dto.setImages(images);
        } else {
            dto.setImages(Collections.emptyList());
        }

        dto.setCategoryName((String) data[5]); // categoryName
        dto.setIsInFlashSale(data[6] != null && (Boolean) data[6]); // isInFlashSale
        dto.setFlashSalePrice(data[7] != null ? (BigDecimal) data[7] : null); // flashSalePrice
        dto.setFlashSaleStockQuantity(data[8] != null ? ((Number) data[8]).intValue() : null); // flashSaleStockQuantity
        dto.setFlashSaleSoldCount(data[9] != null ? ((Number) data[9]).intValue() : null); // flashSaleSoldCount

        // ❌ CÁCH CŨ: Lấy từ query
        // dto.setSoldCount(data[10] != null ? ((Number) data[10]).intValue() : null);
        // // soldCount

        // ✅ CÁCH MỚI: FORCE OVERRIDE giống TrendingBookMapper
        Integer bookId = dto.getId();
        if (bookId != null) {
            Integer realSoldCount = orderDetailRepository.countSoldQuantityByBook(bookId);
            dto.setSoldCount(realSoldCount != null ? realSoldCount : 0);
        } else {
            dto.setSoldCount(0);
        }

        if (dto.getIsInFlashSale()) {
            FlashSaleItem currentFlashSale = flashSaleItemRepository.findActiveFlashSaleByBook(dto.getId());
            if (currentFlashSale != null) {
                int realFlashSaleSoldCount = currentFlashSale.getSoldCount() != null ? currentFlashSale.getSoldCount()
                        : 0;
                dto.setFlashSaleSoldCount(realFlashSaleSoldCount);
            }
        }

        if (dto.getIsInFlashSale() && flashSalePrice != null && dto.getPrice() != null) {
            BigDecimal discount = dto.getPrice().subtract(flashSalePrice);
            BigDecimal discountPercentage = discount.divide(dto.getPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            dto.setDiscountPercentage(discountPercentage.intValue());
            // Giá gốc vẫn giữ nguyên, price sẽ là giá sau discount
            dto.setPrice(flashSalePrice);
            dto.setDiscountActive(true);
        } else {
            dto.setDiscountPercentage(0);
            dto.setDiscountActive(false);
        }
        return dto;
    }

    public static FlashSaleItemBookRequest mapToFlashSaleItemBookRequest(
            Book book,
            FlashSaleItemRepository flashSaleItemRepository,
            OrderDetailRepository orderDetailRepository) {
        if (book == null)
            return null;
        FlashSaleItemBookRequest dto = new FlashSaleItemBookRequest();

        dto.setId(book.getId());
        dto.setBookName(book.getBookName());
        dto.setPrice(book.getPrice());
        dto.setOriginalPrice(book.getPrice());
        dto.setStockQuantity(book.getStockQuantity());

        // Map images
        if (book.getImages() != null && !book.getImages().isEmpty()) {
            List<String> images = Arrays.stream(book.getImages().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            dto.setImages(images);
        } else {
            dto.setImages(Collections.emptyList());
        }

        // Category
        dto.setCategoryName(book.getCategory() != null ? book.getCategory().getCategoryName() : null);

        // Sold count
        Integer soldCount = orderDetailRepository.countSoldQuantityByBook(book.getId());
        dto.setSoldCount(soldCount != null ? soldCount : 0);

        // Discount info
        dto.setDiscountPercentage(book.getDiscountPercent());
        dto.setDiscountActive(book.getDiscountActive() != null ? book.getDiscountActive() : false);

        // Flash sale info
        FlashSaleItem currentFlashSale = flashSaleItemRepository.findActiveFlashSaleByBook(book.getId());
        if (currentFlashSale != null) {
            dto.setIsInFlashSale(true);
            dto.setFlashSalePrice(currentFlashSale.getDiscountPrice());
            dto.setFlashSaleStockQuantity(currentFlashSale.getStockQuantity());
            dto.setFlashSaleSoldCount(currentFlashSale.getSoldCount() != null ? currentFlashSale.getSoldCount() : 0);

            // Nếu có flash sale, tính lại discountPercentage theo giá flash sale
            if (book.getPrice() != null && currentFlashSale.getDiscountPrice() != null) {
                BigDecimal discount = book.getPrice().subtract(currentFlashSale.getDiscountPrice());
                BigDecimal discountPercentage = discount.divide(book.getPrice(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                dto.setDiscountPercentage(discountPercentage.intValue());
                dto.setPrice(currentFlashSale.getDiscountPrice());
                dto.setDiscountActive(true);
            }
        } else {
            dto.setIsInFlashSale(false);
            dto.setFlashSalePrice(null);
            dto.setFlashSaleStockQuantity(null);
            dto.setFlashSaleSoldCount(0);
        }

        return dto;
    }

}
