package org.datn.bookstation.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.entity.CheckoutSession;
import org.datn.bookstation.repository.CheckoutSessionRepository;
import org.datn.bookstation.service.CheckoutSessionService;
import org.datn.bookstation.service.VnPayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;
    private final CheckoutSessionRepository checkoutSessionRepository;
    private final CheckoutSessionService checkoutSessionService;

    /**
     * FE gửi sessionId & userId để lấy link thanh toán VNPAY.
     * Order CHƯA được tạo ở bước này.
     */
    @PostMapping("/vnpay/create-url")
    public ResponseEntity<ApiResponse<String>> createPayUrlForSession(
            @RequestParam Integer sessionId,
            @RequestParam Integer userId,
            HttpServletRequest request) {
        Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty() || !sessionOpt.get().getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, "Không tìm thấy session", null));
        }
        CheckoutSession session = sessionOpt.get();

        if (session.isExpired() || session.getStatus() != 1) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Session đã hết hạn hoặc không còn hiệu lực", null));
        }

        // Sinh TxnRef duy nhất: SES{sessionId}-{timestamp}
        String txnRef = "SES" + sessionId + "-" + System.currentTimeMillis();

        // Tổng tiền
        BigDecimal amount = session.getTotalAmount();

        String payUrl = vnPayService.generatePaymentUrl(amount, txnRef,
                "Thanh toan session " + sessionId, request.getRemoteAddr());

        return ResponseEntity.ok(new ApiResponse<>(200, "OK", payUrl));
    }

    /**
     * Endpoint VNPAY redirect người dùng về (GET)
     */
    @GetMapping("/vnpay-return")
    public void vnpayReturn(@RequestParam Map<String, String> allParams, HttpServletResponse response) throws IOException {
        String failUrl = "http://localhost:5173/order/fail";
        String successUrl = "http://localhost:5173/order/success";
        // 1. Verify checksum
        String receivedHash = allParams.get("vnp_SecureHash");
        boolean valid = vnPayService.validateChecksum(
                allParams.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                receivedHash);

        if (!valid) {
            response.sendRedirect(failUrl + "?status=failed&reason=checksum");
            return;
        }

        String responseCode = allParams.get("vnp_ResponseCode");
        String txnRef       = allParams.get("vnp_TxnRef");

        // Expect format SES{sessionId}-{timestamp}
        if (txnRef == null || !txnRef.startsWith("SES")) {
            response.sendRedirect(failUrl + "?status=failed&reason=txnref");
            return;
        }

        // Parse sessionId
        String idPart = txnRef.substring(3);
        if (idPart.contains("-")) idPart = idPart.substring(0, idPart.indexOf('-'));

        Integer sessionId;
        try {
            sessionId = Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            response.sendRedirect(failUrl + "?status=failed&reason=badSessionId");
            return;
        }

        if (!"00".equals(responseCode)) {
            // Payment failed
            response.sendRedirect(failUrl + "?status=failed&txnRef=" + txnRef);
            return;
        }

        // Payment success – create order
        Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            response.sendRedirect(failUrl + "?status=failed&reason=sessionNotFound");
            return;
        }

        Integer userId = sessionOpt.get().getUser().getId();
        
        // ✅ UPDATE PAYMENT METHOD TO VNPAY BEFORE CREATING ORDER
        checkoutSessionService.updateSessionPaymentMethod(sessionId, "VNPay");
        
        ApiResponse<String> orderResp = checkoutSessionService.createOrderFromSession(sessionId, userId);

        if (orderResp == null || orderResp.getStatus() != 201) {
            response.sendRedirect(failUrl + "?status=failed&reason=order");
            return;
        }

        String orderId = orderResp.getData();
        if (orderId == null) orderId = "";

        String redirectUrl = successUrl + "/" + orderId;
        response.sendRedirect(redirectUrl);
    }

    /**
     * VNPAY gọi server-to-server để thông báo kết quả (IPN)
     */
    @PostMapping("/vnpay-ipn")
    public ResponseEntity<String> vnpayIpn(@RequestParam Map<String, String> allParams) {
        String receivedHash = allParams.get("vnp_SecureHash");
        boolean valid = vnPayService.validateChecksum(allParams.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)), receivedHash);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Checksum không hợp lệ");
        }

        String responseCode = allParams.get("vnp_ResponseCode");
        String txnRef = allParams.get("vnp_TxnRef");

        if (!txnRef.startsWith("SES")) {
            return ResponseEntity.ok("IGNORED");
        }

        // Extract sessionId
        String idPart = txnRef.substring(3);
        Integer sessionId;
        if (idPart.contains("-")) {
            idPart = idPart.substring(0, idPart.indexOf('-'));
        }
        try {
            sessionId = Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid txnRef");
        }

        Optional<CheckoutSession> sessionOpt = checkoutSessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.ok("SESSION_NOT_FOUND");
        }

        if ("00".equals(responseCode)) {
            // ✅ UPDATE PAYMENT METHOD TO VNPAY BEFORE CREATING ORDER
            checkoutSessionService.updateSessionPaymentMethod(sessionId, "VNPay");
            
            // Tạo order từ session
            checkoutSessionService.createOrderFromSession(sessionId, sessionOpt.get().getUser().getId());
        }
        return ResponseEntity.ok("OK");
    }
} 