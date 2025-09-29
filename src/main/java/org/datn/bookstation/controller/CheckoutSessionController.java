package org.datn.bookstation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.CheckoutSessionRequest;
import org.datn.bookstation.dto.request.CreateOrderFromSessionRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CheckoutSessionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.service.CheckoutSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout-sessions")
@RequiredArgsConstructor
@Slf4j
public class CheckoutSessionController {

    private final CheckoutSessionService checkoutSessionService;

    /**
     * Tạo checkout session mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createCheckoutSession(
            @Valid @RequestBody CheckoutSessionRequest request,
            @RequestParam Integer userId) {
        log.info("Creating checkout session for user: {}", userId);
        ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.createCheckoutSession(userId, request);
        HttpStatus status = response.getStatus() == 201 ? HttpStatus.CREATED : 
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Tạo checkout session từ giỏ hàng
     */
    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createSessionFromCart(
            @RequestParam Integer userId) {
        log.info("Creating checkout session from cart for user: {}", userId);
        ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.createSessionFromCart(userId);
        HttpStatus status = response.getStatus() == 201 ? HttpStatus.CREATED : 
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Cập nhật checkout session
     */
    @PutMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> updateCheckoutSession(
            @PathVariable Integer sessionId,
            @Valid @RequestBody CheckoutSessionRequest request,
            @RequestParam Integer userId) {
        log.info("Updating checkout session: {} for user: {}", sessionId, userId);
        ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.updateCheckoutSession(sessionId, userId, request);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Lấy checkout session theo ID
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> getCheckoutSessionById(
            @PathVariable Integer sessionId,
            @RequestParam Integer userId) {
        ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.getCheckoutSessionById(sessionId, userId);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Lấy checkout session mới nhất của user
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> getLatestCheckoutSession(
            @RequestParam Integer userId) {
        ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.getLatestCheckoutSession(userId);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Lấy danh sách checkout sessions của user
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<PaginationResponse<CheckoutSessionResponse>>> getUserCheckoutSessions(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ApiResponse<PaginationResponse<CheckoutSessionResponse>> response = 
                checkoutSessionService.getUserCheckoutSessions(userId, page, size);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Lấy tất cả checkout sessions (Admin)
     */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<PaginationResponse<CheckoutSessionResponse>>> getAllCheckoutSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate) {
        ApiResponse<PaginationResponse<CheckoutSessionResponse>> response = 
                checkoutSessionService.getAllCheckoutSessions(page, size, userId, status, startDate, endDate);
        HttpStatus status_code = response.getStatus() == 200 ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status_code).body(response);
    }

    /**
     * Xóa checkout session
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<String>> deleteCheckoutSession(
            @PathVariable Integer sessionId,
            @RequestParam Integer userId) {
        log.info("Deleting checkout session: {} for user: {}", sessionId, userId);
        ApiResponse<String> response = checkoutSessionService.deleteCheckoutSession(sessionId, userId);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Đánh dấu session hoàn thành
     */
    @PatchMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> markSessionCompleted(
            @PathVariable Integer sessionId,
            @RequestParam Integer userId) {
        ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.markSessionCompleted(sessionId, userId);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Tính lại giá cho session
     */
    @PatchMapping("/{sessionId}/recalculate")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> recalculateSessionPricing(
            @PathVariable Integer sessionId,
            @RequestParam Integer userId) {
        ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.recalculateSessionPricing(sessionId, userId);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Validate session
     */
    @PostMapping("/{sessionId}/validate")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> validateSession(
            @PathVariable Integer sessionId,
            @RequestParam Integer userId) {
        ApiResponse<CheckoutSessionResponse> response = checkoutSessionService.validateSession(sessionId, userId);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND :
                           response.getStatus() == 400 ? HttpStatus.BAD_REQUEST : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Tạo đơn hàng từ checkout session với price validation
     */
    @PostMapping("/{sessionId}/create-order")
    public ResponseEntity<ApiResponse<String>> createOrderFromSession(
            @PathVariable Integer sessionId,
            @RequestParam Integer userId,
            @Valid @RequestBody CreateOrderFromSessionRequest request) {
        try {
            log.info(" Creating order from checkout session: {} for user: {} with price validation", sessionId, userId);
            
            // Input validation
            if (sessionId == null || sessionId <= 0) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(400, "Session ID không hợp lệ", null)
                );
            }
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(
                    new ApiResponse<>(400, "User ID không hợp lệ", null)
                );
            }
            
            ApiResponse<String> response = checkoutSessionService.createOrderFromSession(sessionId, userId, request);
            
            // Enhanced status mapping
            HttpStatus status;
            switch (response.getStatus()) {
                case 201:
                    status = HttpStatus.CREATED;
                    break;
                case 404:
                    status = HttpStatus.NOT_FOUND;
                    break;
                case 400:
                    status = HttpStatus.BAD_REQUEST;
                    break;
                case 409:
                    status = HttpStatus.CONFLICT; // Price changed
                    break;
                case 500:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    break;
                default:
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    log.warn("Unexpected status code from service: {}", response.getStatus());
            }
            
            // Log result for monitoring
            if (response.getStatus() == 201) {
                log.info(" Successfully created order from session {} for user {}: {}", 
                    sessionId, userId, response.getData());
            } else {
                log.warn(" Failed to create order from session {} for user {}: {} - {}", 
                    sessionId, userId, response.getStatus(), response.getMessage());
            }
            
            return ResponseEntity.status(status).body(response);
            
        } catch (Exception e) {
            log.error(" Unexpected error in createOrderFromSession controller for session {} user {}: {}", 
                sessionId, userId, e.getMessage(), e);
            
            ApiResponse<String> errorResponse = new ApiResponse<>(
                500, 
                "Lỗi hệ thống không mong muốn. Vui lòng thử lại sau.", 
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Gia hạn thời gian hết hạn của session
     */
    @PatchMapping("/{sessionId}/extend")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> extendSessionExpiry(
            @PathVariable Integer sessionId,
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "30") Long additionalMinutes) {
        ApiResponse<CheckoutSessionResponse> response = 
                checkoutSessionService.extendSessionExpiry(sessionId, userId, additionalMinutes);
        HttpStatus status = response.getStatus() == 200 ? HttpStatus.OK : 
                           response.getStatus() == 404 ? HttpStatus.NOT_FOUND : 
                           HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Cleanup expired sessions (Admin endpoint)
     */
    @PostMapping("/admin/cleanup")
    public ResponseEntity<ApiResponse<Integer>> cleanupExpiredSessions() {
        log.info("Admin triggered cleanup of expired checkout sessions");
        int cleanedUp = checkoutSessionService.cleanupExpiredSessions();
        return ResponseEntity.ok(new ApiResponse<>(200, "Cleanup hoàn thành, đã xử lý " + cleanedUp + " sessions", cleanedUp));
    }
}
