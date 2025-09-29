package org.datn.bookstation.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.CounterSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CounterSaleResponse;
import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.service.CounterSaleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * ✅ Controller cho bán hàng tại quầy (Counter Sales)
 * 
 * Tính năng:
 * 1. Tạo đơn hàng tại quầy (không cần địa chỉ, có thể không cần tài khoản)
 * 2. Hủy đơn hàng tại quầy 
 * 3. Xem chi tiết đơn hàng tại quầy
 * 4. Tính toán giá tại quầy (preview)
 * 
 * Đặc điểm:
 * - OrderType = "COUNTER"
 * - Không cần userId (có thể bán cho khách vãng lai)
 * - Chỉ cần customerName + customerPhone
 * - Không cần addressId (bán tại quầy)
 * - Vẫn áp dụng voucher, flash sale như bình thường
 * - Thanh toán ngay tại quầy (CASH/CARD/BANK_TRANSFER)
 * - Trạng thái mặc định: DELIVERED (đã thanh toán và nhận hàng ngay)
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/counter-sales")
@Slf4j
public class CounterSaleController {
    
    private final CounterSaleService counterSaleService;
    
    /**
     * 🔥 API tạo đơn hàng tại quầy
     * POST /api/counter-sales
     * 
     * Luồng:
     * 1. Validate sản phẩm, số lượng, giá
     * 2. Áp dụng voucher nếu có
     * 3. Tạo đơn hàng với trạng thái DELIVERED
     * 4. Trừ stock, cộng sold count ngay lập tức
     * 5. Trả về thông tin đơn hàng
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CounterSaleResponse>> createCounterSale(
            @Valid @RequestBody CounterSaleRequest request) {
        
        log.info("🎯 Counter-sales endpoint hit with request: {}", request.getCustomerName());
        
        ApiResponse<CounterSaleResponse> response = counterSaleService.createCounterSale(request);
        
        log.info("🎯 Counter-sales service returned status: {}", response.getStatus());
        
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.CREATED :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST :
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 🔥 API tính toán giá tại quầy (preview)
     * POST /api/counter-sales/calculate
     * 
     * Tính toán trước khi tạo đơn hàng thực sự
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<CounterSaleResponse>> calculateCounterSale(
            @Valid @RequestBody CounterSaleRequest request) {
        
        ApiResponse<CounterSaleResponse> response = counterSaleService.calculateCounterSale(request);
        
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST :
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 🔥 API xem chi tiết đơn hàng tại quầy
     * GET /api/counter-sales/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getCounterSaleDetails(
            @PathVariable Integer orderId) {
        
        ApiResponse<OrderResponse> response = counterSaleService.getCounterSaleDetails(orderId);
        
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK :
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * 🔥 API hủy đơn hàng tại quầy
     * PATCH /api/counter-sales/{orderId}/cancel
     * 
     * Chỉ có thể hủy trong vòng 24h sau khi tạo
     * Sẽ hoàn stock về kho
     */
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelCounterSale(
            @PathVariable Integer orderId,
            @RequestParam Integer staffId,
            @RequestParam(required = false) String reason) {
        
        ApiResponse<OrderResponse> response = counterSaleService.cancelCounterSale(orderId, staffId, reason);
        
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST :
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           HttpStatus.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity.status(status).body(response);
    }
}
