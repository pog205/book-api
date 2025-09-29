package org.datn.bookstation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.request.RefundRequestCreate;
import org.datn.bookstation.dto.request.RefundApprovalRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.RefundRequestResponse;

import org.datn.bookstation.service.RefundService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ REFUND CONTROLLER - Quản lý yêu cầu hoàn trả
 * 
 * API Endpoints:
 * - GET /api/refunds/pending - Admin lấy danh sách yêu cầu hoàn trả chờ phê duyệt
 * - GET /api/refunds/user/{userId} - Lấy danh sách yêu cầu hoàn trả của user
 * - GET /api/refunds/{id} - Lấy chi tiết yêu cầu hoàn trả
 * - POST /api/refunds - Tạo yêu cầu hoàn trả mới
 * - POST /api/refunds/{id}/approve - Admin phê duyệt yêu cầu hoàn trả
 * - POST /api/refunds/{id}/reject - Admin từ chối yêu cầu hoàn trả
 * - POST /api/refunds/{id}/process - Admin xử lý hoàn trả sau khi phê duyệt
 * - GET /api/refunds/validate/{orderId}/{userId} - Validate yêu cầu hoàn trả
 */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {
    
    private final RefundService refundService;
    
    /**
     * ✅ API: Lấy danh sách yêu cầu hoàn trả chờ phê duyệt (Admin)
     * GET /api/refunds/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getPendingRefundRequests() {
        try {
            List<RefundRequestResponse> requests = refundService.getPendingRefundRequests();
            
            ApiResponse<List<RefundRequestResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy danh sách yêu cầu hoàn trả chờ phê duyệt thành công",
                requests
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<RefundRequestResponse>> response = new ApiResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Lỗi khi lấy danh sách yêu cầu hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * ✅ API: Lấy danh sách yêu cầu hoàn trả của user
     * GET /api/refunds/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getRefundRequestsByUser(
            @PathVariable Integer userId) {
        try {
            List<RefundRequestResponse> requests = refundService.getRefundRequestsByUser(userId);
            
            ApiResponse<List<RefundRequestResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy danh sách yêu cầu hoàn trả của người dùng thành công",
                requests
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<RefundRequestResponse>> response = new ApiResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Lỗi khi lấy danh sách yêu cầu hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * ✅ API: Lấy chi tiết yêu cầu hoàn trả
     * GET /api/refunds/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> getRefundRequestById(
            @PathVariable Integer id) {
        try {
            RefundRequestResponse request = refundService.getRefundRequestById(id);
            
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy chi tiết yêu cầu hoàn trả thành công",
                request
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.NOT_FOUND.value(),
                "Không tìm thấy yêu cầu hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * ✅ API: Admin lấy chi tiết đầy đủ yêu cầu hoàn trả (bao gồm thông tin đơn hàng, voucher, lý do, ...)
     * GET /api/refunds/{id}/admin-detail
     */
    @GetMapping("/{id}/admin-detail")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> getRefundAdminDetail(
            @PathVariable Integer id) {
        try {
            RefundRequestResponse request = refundService.getRefundRequestById(id);
            
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy chi tiết admin yêu cầu hoàn trả thành công",
                request
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.NOT_FOUND.value(),
                "Không tìm thấy yêu cầu hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * ✅ API: Tạo yêu cầu hoàn trả mới
     * POST /api/refunds?userId={userId}
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RefundRequestResponse>> createRefundRequest(
            @Valid @RequestBody RefundRequestCreate request,
            @RequestParam Integer userId) {
        try {
            RefundRequestResponse refundRequest = refundService.createRefundRequest(request, userId);
            
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.CREATED.value(),
                "Tạo yêu cầu hoàn trả thành công",
                refundRequest
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Lỗi khi tạo yêu cầu hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * ✅ API: Admin phê duyệt yêu cầu hoàn trả
     * POST /api/refunds/{id}/approve?adminId={adminId}
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> approveRefundRequest(
            @PathVariable Integer id,
            @RequestParam Integer adminId,
            @Valid @RequestBody RefundApprovalRequest approval) {
        try {
            RefundRequestResponse refundRequest = refundService.approveRefundRequest(id, approval, adminId);
            
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Phê duyệt yêu cầu hoàn trả thành công",
                refundRequest
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Lỗi khi phê duyệt yêu cầu hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * ✅ API: Admin từ chối yêu cầu hoàn trả
     * POST /api/refunds/{id}/reject?adminId={adminId}
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> rejectRefundRequest(
            @PathVariable Integer id,
            @RequestParam Integer adminId,
            @Valid @RequestBody RefundApprovalRequest rejection) {
        try {
            RefundRequestResponse refundRequest = refundService.rejectRefundRequest(id, rejection, adminId);
            
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Từ chối yêu cầu hoàn trả thành công",
                refundRequest
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Lỗi khi từ chối yêu cầu hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * ✅ API: Admin xử lý hoàn trả sau khi phê duyệt
     * POST /api/refunds/{id}/process?adminId={adminId}
     * 
     * ⚠️ ENHANCED v2.0: API này giờ tự động set Order status thành:
     * - REFUNDED (nếu RefundType = FULL) 
     * - PARTIALLY_REFUNDED (nếu RefundType = PARTIAL)
     * Frontend KHÔNG cần gọi thêm API status-transition
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> processRefund(
            @PathVariable Integer id,
            @RequestParam Integer adminId) {
        try {
            RefundRequestResponse refundRequest = refundService.processRefund(id, adminId);
            
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Xử lý hoàn trả thành công",
                refundRequest
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<RefundRequestResponse> response = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Lỗi khi xử lý hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * ✅ API: Validate yêu cầu hoàn trả
     * GET /api/refunds/validate/{orderId}/{userId}
     */
    @GetMapping("/validate/{orderId}/{userId}")
    public ResponseEntity<ApiResponse<String>> validateRefundRequest(
            @PathVariable Integer orderId,
            @PathVariable Integer userId) {
        try {
            String validationResult = refundService.validateRefundRequest(orderId, userId);
            
            if (validationResult == null) {
                ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Đơn hàng có thể được hoàn trả",
                    "VALID"
                );
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    validationResult,
                    "INVALID"
                );
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Lỗi khi validate yêu cầu hoàn trả: " + e.getMessage(),
                "ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ✅ API: Lấy tất cả yêu cầu hoàn trả (Admin, có phân trang, sort)
     * GET /api/refunds/all?page=0&size=20&sortBy=createdAt&sortDir=desc
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getAllRefundRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            List<RefundRequestResponse> requests = refundService.getAllRefundRequests(page, size, sortBy, sortDir);
            ApiResponse<List<RefundRequestResponse>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Lấy danh sách tất cả yêu cầu hoàn trả thành công",
                requests
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<RefundRequestResponse>> response = new ApiResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Lỗi khi lấy danh sách hoàn trả: " + e.getMessage(),
                null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
