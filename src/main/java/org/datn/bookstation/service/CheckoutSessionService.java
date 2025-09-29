package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.CheckoutSessionRequest;
import org.datn.bookstation.dto.request.CreateOrderFromSessionRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CheckoutSessionResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.entity.CheckoutSession;


public interface CheckoutSessionService {

    /**
     * Tạo checkout session mới
     */
    ApiResponse<CheckoutSessionResponse> createCheckoutSession(Integer userId, CheckoutSessionRequest request);

    /**
     * Cập nhật checkout session
     */
    ApiResponse<CheckoutSessionResponse> updateCheckoutSession(Integer sessionId, Integer userId, CheckoutSessionRequest request);

    /**
     * Lấy checkout session theo ID (với kiểm tra user)
     */
    ApiResponse<CheckoutSessionResponse> getCheckoutSessionById(Integer sessionId, Integer userId);

    /**
     * Lấy checkout session mới nhất của user
     */
    ApiResponse<CheckoutSessionResponse> getLatestCheckoutSession(Integer userId);

    /**
     * Lấy tất cả checkout sessions của user (có phân trang)
     */
    ApiResponse<PaginationResponse<CheckoutSessionResponse>> getUserCheckoutSessions(
            Integer userId, int page, int size);

    /**
     * Lấy tất cả checkout sessions (Admin) 
     */
    ApiResponse<PaginationResponse<CheckoutSessionResponse>> getAllCheckoutSessions(
            int page, int size, Integer userId, Byte status, Long startDate, Long endDate);

    /**
     * Xóa checkout session
     */
    ApiResponse<String> deleteCheckoutSession(Integer sessionId, Integer userId);

    /**
     * Đánh dấu session đã hoàn thành (sau khi tạo order thành công)
     */
    ApiResponse<CheckoutSessionResponse> markSessionCompleted(Integer sessionId, Integer userId);

    /**
     * Tính toán lại giá của session (kiểm tra flash sale, voucher hết hạn)
     */
    ApiResponse<CheckoutSessionResponse> recalculateSessionPricing(Integer sessionId, Integer userId);

    /**
     * Validate session trước khi checkout
     */
    ApiResponse<CheckoutSessionResponse> validateSession(Integer sessionId, Integer userId);

    /**
     * Tạo order từ checkout session với price validation  
     */
    ApiResponse<String> createOrderFromSession(Integer sessionId, Integer userId, CreateOrderFromSessionRequest request);

    /**
     * Tạo order từ checkout session (backward compatibility) 
     */
    ApiResponse<String> createOrderFromSession(Integer sessionId, Integer userId);

    /**
     * Dọn dẹp sessions hết hạn (scheduled task)
     */
    int cleanupExpiredSessions();

    /**
     * Extend thời gian hết hạn của session
     */
    ApiResponse<CheckoutSessionResponse> extendSessionExpiry(Integer sessionId, Integer userId, Long additionalMinutes);

    /**
     * Copy cart hiện tại thành checkout session
     */
    ApiResponse<CheckoutSessionResponse> createSessionFromCart(Integer userId);

    /**
     * Lấy entity cho internal use
     */
    CheckoutSession getSessionEntity(Integer sessionId, Integer userId);

    /**
     *  NEW: Update payment method cho session
     */
    ApiResponse<String> updateSessionPaymentMethod(Integer sessionId, String paymentMethod);
}
