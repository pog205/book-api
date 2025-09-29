package org.datn.bookstation.controller;

import org.datn.bookstation.dto.request.FlashSaleItemRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleItemResponse;
import org.datn.bookstation.dto.response.FlashSaleItemStatsResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.service.FlashSaleItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/flash-sales/{flashSaleId}/items")
public class FlashSaleItemController {

    @Autowired
    private FlashSaleItemService flashSaleItemService;

    @GetMapping()
    public ApiResponse<PaginationResponse<FlashSaleItemResponse>> getAllByFlashSale(
            @PathVariable Integer flashSaleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minPercent,
            @RequestParam(required = false) BigDecimal maxPercent,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) Integer maxQuantity) {
        return flashSaleItemService.getAllWithFilter(page, size, flashSaleId, bookName, status,
                minPrice, maxPrice, minPercent, maxPercent, minQuantity, maxQuantity);
    }

    @PostMapping
    public ApiResponse<FlashSaleItemResponse> create(@RequestBody FlashSaleItemRequest request) {
        return flashSaleItemService.create(request);
    }

    @PutMapping("/{itemId}")
    public ApiResponse<FlashSaleItemResponse> update(@PathVariable Integer itemId,
            @RequestBody FlashSaleItemRequest request) {
        return flashSaleItemService.update(itemId, request);
    }

    @PatchMapping("/{itemId}/status")
    public ApiResponse<FlashSaleItemResponse> toggleStatus(@PathVariable Integer itemId) {
        return flashSaleItemService.toggleStatus(itemId);
    }

    @GetMapping("/stats")
    public ApiResponse<FlashSaleItemStatsResponse> getFlashSaleStats() {
        return flashSaleItemService.getFlashSaleStats();
    }

}