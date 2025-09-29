package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.BookSentimentResponse;
import org.datn.bookstation.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 📊 Mapper để chuyển đổi Book thành BookSentimentResponse với thông tin sentiment
 */
@Component
public class BookSentimentMapper {

    @Autowired
    private BookResponseMapper bookResponseMapper;

    /**
     * Chuyển đổi Book thành BookSentimentResponse với sentiment stats
     */
    public BookSentimentResponse toSentimentResponse(Book book, Map<Integer, Object[]> sentimentStatsMap) {
        // Lấy thông tin cơ bản từ BookResponseMapper
        var basicResponse = bookResponseMapper.toResponse(book);
        
        // Lấy sentiment stats cho book này
        Object[] sentimentData = sentimentStatsMap.get(book.getId());
        BookSentimentResponse.SentimentStats sentimentStats;
        
        if (sentimentData != null) {
            sentimentStats = BookSentimentResponse.SentimentStats.builder()
                    .positivePercentage(sentimentData[1] != null ? ((Number) sentimentData[1]).doubleValue() : 0.0)
                    .averageRating(sentimentData[2] != null ? ((Number) sentimentData[2]).doubleValue() : 0.0)
                    .totalReviews(sentimentData[3] != null ? ((Number) sentimentData[3]).intValue() : 0)
                    .positiveReviews(sentimentData[4] != null ? ((Number) sentimentData[4]).intValue() : 0)
                    .negativeReviews(sentimentData[5] != null ? ((Number) sentimentData[5]).intValue() : 0)
                    .ratingDistribution(BookSentimentResponse.RatingDistribution.builder()
                            .rating1Count(sentimentData[6] != null ? ((Number) sentimentData[6]).intValue() : 0)
                            .rating2Count(sentimentData[7] != null ? ((Number) sentimentData[7]).intValue() : 0)
                            .rating3Count(sentimentData[8] != null ? ((Number) sentimentData[8]).intValue() : 0)
                            .rating4Count(sentimentData[9] != null ? ((Number) sentimentData[9]).intValue() : 0)
                            .rating5Count(sentimentData[10] != null ? ((Number) sentimentData[10]).intValue() : 0)
                            .build())
                    .build();
        } else {
            // Trường hợp không có sentiment data (fallback)
            sentimentStats = BookSentimentResponse.SentimentStats.builder()
                    .positivePercentage(0.0)
                    .averageRating(0.0)
                    .totalReviews(0)
                    .positiveReviews(0)
                    .negativeReviews(0)
                    .ratingDistribution(BookSentimentResponse.RatingDistribution.builder()
                            .rating1Count(0)
                            .rating2Count(0)
                            .rating3Count(0)
                            .rating4Count(0)
                            .rating5Count(0)
                            .build())
                    .build();
        }
        
        // Tạo BookSentimentResponse với tất cả thông tin
        return BookSentimentResponse.builder()
                .id(basicResponse.getId())
                .bookName(basicResponse.getBookName())
                .description(basicResponse.getDescription())
                .price(basicResponse.getPrice())
                .stockQuantity(basicResponse.getStockQuantity())
                .publicationDate(basicResponse.getPublicationDate())
                .categoryName(basicResponse.getCategoryName())
                .categoryId(basicResponse.getCategoryId())
                .supplierName(basicResponse.getSupplierName())
                .supplierId(basicResponse.getSupplierId())
                .bookCode(basicResponse.getBookCode())
                .status(basicResponse.getStatus())
                .createdAt(basicResponse.getCreatedAt())
                .updatedAt(basicResponse.getUpdatedAt())
                .authors(basicResponse.getAuthors())
                .publisherName(basicResponse.getPublisherName())
                .publisherId(basicResponse.getPublisherId())
                .coverImageUrl(basicResponse.getCoverImageUrl())
                .translator(basicResponse.getTranslator())
                .isbn(basicResponse.getIsbn())
                .pageCount(basicResponse.getPageCount())
                .language(basicResponse.getLanguage())
                .weight(basicResponse.getWeight())
                .dimensions(basicResponse.getDimensions())
                .images(basicResponse.getImages())
                .soldCount(basicResponse.getSoldCount())
                .processingQuantity(basicResponse.getProcessingQuantity())
                .discountValue(basicResponse.getDiscountValue())
                .discountPercent(basicResponse.getDiscountPercent())
                .discountActive(basicResponse.getDiscountActive())
                .isInFlashSale(basicResponse.getIsInFlashSale())
                .flashSalePrice(basicResponse.getFlashSalePrice())
                .flashSaleStock(basicResponse.getFlashSaleStock())
                .flashSaleSoldCount(basicResponse.getFlashSaleSoldCount())
                .flashSaleEndTime(basicResponse.getFlashSaleEndTime())
                .sentimentStats(sentimentStats)
                .build();
    }
}
