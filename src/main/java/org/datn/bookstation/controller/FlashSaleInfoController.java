package org.datn.bookstation.controller;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.FlashSaleInfoResponse;
import org.datn.bookstation.service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flash-sales")
public class FlashSaleInfoController {
    
    @Autowired
    private FlashSaleService flashSaleService;
    
    /**
     * Lấy flash sale đang active cho một sách
     * Business rule: 1 sách chỉ có 1 flash sale active tại 1 thời điểm
     */
    @GetMapping("/book/{bookId}/active")
    public ResponseEntity<ApiResponse<FlashSaleInfoResponse>> getActiveFlashSaleForBook(@PathVariable Long bookId) {
        FlashSaleInfoResponse response = flashSaleService.getActiveFlashSaleInfo(bookId);
        
        if (response != null) {
            return ResponseEntity.ok(new ApiResponse<>(200, "Flash sale đang active cho sách", response));
        } else {
            return ResponseEntity.ok(new ApiResponse<>(404, "Không có flash sale đang active cho sách này", null));
        }
    }
    
    /**
     * Kiểm tra flash sale có hợp lệ không
     */
    @GetMapping("/item/{flashSaleItemId}/valid")
    public ResponseEntity<ApiResponse<Boolean>> checkFlashSaleValid(@PathVariable Long flashSaleItemId) {
        boolean isValid = flashSaleService.isFlashSaleValid(flashSaleItemId);
        
        return ResponseEntity.ok(new ApiResponse<>(200, isValid ? "Flash sale hợp lệ" : "Flash sale không hợp lệ", isValid));
    }
    
    /**
     * Kiểm tra flash sale có đủ stock không
     */
    @GetMapping("/item/{flashSaleItemId}/stock")
    public ResponseEntity<ApiResponse<Boolean>> checkFlashSaleStock(
            @PathVariable Long flashSaleItemId,
            @RequestParam Integer quantity) {
        
        boolean hasStock = flashSaleService.hasEnoughStock(flashSaleItemId, quantity);
        
        return ResponseEntity.ok(new ApiResponse<>(200, hasStock ? "Đủ stock" : "Không đủ stock", hasStock));
    }
}
