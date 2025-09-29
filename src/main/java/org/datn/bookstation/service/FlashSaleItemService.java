package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.FlashSaleItemBookRequest;
import org.datn.bookstation.dto.request.FlashSaleItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleItemResponse;
import org.datn.bookstation.dto.response.FlashSaleItemStatsResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.FlashSaleItem;

import java.math.BigDecimal;
import java.util.List;

public interface FlashSaleItemService {
    ApiResponse<PaginationResponse<FlashSaleItemResponse>> getAllWithFilter(int page, int size, Integer flashSaleId,
            String bookName, Byte status,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minPercent, BigDecimal maxPercent,
            Integer minQuantity, Integer maxQuantity);

    ApiResponse<FlashSaleItemResponse> create(FlashSaleItemRequest request);

    ApiResponse<FlashSaleItemResponse> update(Integer id, FlashSaleItemRequest request);

    ApiResponse<FlashSaleItemResponse> toggleStatus(Integer id);

    ApiResponse<List<FlashSaleItemBookRequest>> findAllBooksInActiveFlashSale();

    /**
     * Tìm flash sale item đang active theo bookId
     */
    FlashSaleItem findActiveFlashSaleByBook(Integer bookId);

    ApiResponse<FlashSaleItemStatsResponse> getFlashSaleStats();
}