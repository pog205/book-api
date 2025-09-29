package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.AuthorResponse;
import org.datn.bookstation.dto.response.BookResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.service.BookProcessingQuantityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookResponseMapper {
    
    @Autowired
    private FlashSaleItemRepository flashSaleItemRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private BookProcessingQuantityService bookProcessingQuantityService;
    
    public BookResponse toResponse(Book book) {
        if (book == null) return null;
        
        BookResponse response = new BookResponse();
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
        
        // Set new book detail fields
        response.setCoverImageUrl(book.getCoverImageUrl());
        response.setTranslator(book.getTranslator());
        response.setIsbn(book.getIsbn());
        response.setPageCount(book.getPageCount());
        response.setLanguage(book.getLanguage());
        response.setWeight(book.getWeight());
        response.setDimensions(book.getDimensions());
        
        // Set category info
        if (book.getCategory() != null) {
            response.setCategoryId(book.getCategory().getId());
            response.setCategoryName(book.getCategory().getCategoryName());
        }
        
        // Set supplier info
        if (book.getSupplier() != null) {
            response.setSupplierId(book.getSupplier().getId());
            response.setSupplierName(book.getSupplier().getSupplierName());
        }
        
        // Set publisher info
        if (book.getPublisher() != null) {
            response.setPublisherId(book.getPublisher().getId());
            response.setPublisherName(book.getPublisher().getPublisherName());
        }
        
        // Set authors info
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

        // Set images (nhiều ảnh)
        if (book.getImages() != null && !book.getImages().isEmpty()) {
            List<String> images = java.util.Arrays.stream(book.getImages().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            response.setImages(images);
        } else {
            response.setImages(java.util.Collections.emptyList());
        }
        
        // ✅ ADMIN CẦN: Tính số lượng đã bán
        Integer totalSold = orderDetailRepository.countSoldQuantityByBook(book.getId());


        response.setSoldCount(totalSold != null ? totalSold : 0);
        // ✅ ADMIN CẦN: Thông tin discount của book
        response.setDiscountValue(book.getDiscountValue());
        response.setDiscountPercent(book.getDiscountPercent());
        System.out.println("getDiscountPercent"+book.getDiscountPercent());
        // ✅ Thêm discountActive vào response
        response.setDiscountActive(book.getDiscountActive());
        // ✅ SỬA: Trả về soldCount từ Book entity
        response.setSoldCount(book.getSoldCount() != null ? book.getSoldCount() : 0);
        // ✅ THÊM MỚI: Processing quantity real-time
        response.setProcessingQuantity(bookProcessingQuantityService.getProcessingQuantity(book.getId()));
        // ✅ ADMIN CẦN: Kiểm tra Flash Sale hiện tại
        FlashSaleItem currentFlashSale = flashSaleItemRepository.findActiveFlashSaleByBook(book.getId());
        System.out.println(currentFlashSale);
        if (currentFlashSale != null) {
            response.setIsInFlashSale(true);
            response.setFlashSalePrice(currentFlashSale.getDiscountPrice()); // discountPrice là giá sau giảm giá
            response.setFlashSaleStock(currentFlashSale.getStockQuantity()); // ✅ THÊM: Số lượng flash sale còn lại
            response.setFlashSaleEndTime(currentFlashSale.getFlashSale().getEndTime());
            
            // ✅ FIX: Dùng trực tiếp field soldCount thay vì query phức tạp
            response.setFlashSaleSoldCount(currentFlashSale.getSoldCount() != null ? currentFlashSale.getSoldCount() : 0);
        } else {
            response.setIsInFlashSale(false);
            response.setFlashSalePrice(null);
            response.setFlashSaleStock(null); // ✅ THÊM
            response.setFlashSaleSoldCount(0);
            response.setFlashSaleEndTime(null);
        }
        
        return response;
    }
}
