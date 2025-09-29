package org.datn.bookstation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.datn.bookstation.service.FlashSaleService;
import org.datn.bookstation.dto.request.FlashSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.FlashSaleResponse;
import org.datn.bookstation.dto.response.FlashSaleDisplayResponse;
import org.datn.bookstation.dto.response.FlashSaleStatsResponse;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/flash-sales")
public class FlashSaleController {
    @Autowired
    private FlashSaleService flashSaleService;

    // @GetMapping
    // public ApiResponse<PaginationResponse<FlashSaleResponse>>
    // getAllFlashSaleWithPagination(@RequestParam int page, @RequestParam int size)
    // {
    // return flashSaleService.getAllFlashSaleWithPagination(page, size);
    // }

    @GetMapping
    public ApiResponse<PaginationResponse<FlashSaleResponse>> getAllWithFilter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(required = false) Byte status) {
        return flashSaleService.getAllWithFilter(page, size, name, from, to, status);
    }

    @PostMapping
    public ApiResponse<FlashSaleResponse> createFlashSale(@RequestBody FlashSaleRequest request) {
        return flashSaleService.createFlashSale(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<FlashSaleResponse> updateFlashSale(@RequestBody FlashSaleRequest request,
            @PathVariable Integer id) {
        return flashSaleService.updateFlashSale(request, id);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<FlashSaleResponse> toggleStatus(@PathVariable Integer id) {
        return flashSaleService.toggleStatus(id);
    }

    @GetMapping("/today")
    public ApiResponse<FlashSaleDisplayResponse> getTodayFlashSale() {
        return flashSaleService.findFlashSalesByDate();
    }

    @GetMapping("/stats")
    public ApiResponse<FlashSaleStatsResponse> getFlashSaleStats() {
        return flashSaleService.getFlashSaleStats();
    }
}
