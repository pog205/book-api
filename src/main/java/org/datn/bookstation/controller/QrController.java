package org.datn.bookstation.controller;

import org.datn.bookstation.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class QrController {
    @GetMapping("/api/qr")
    public ApiResponse<String> generateQr(@RequestParam(required = false) String amount,
            @RequestParam(required = false) String addInfo,
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String bankCode) {

        try {
            String finalBankCode = (bankCode != null && !bankCode.isEmpty()) ? bankCode : "970418";
            String finalAccountNumber = (accountNumber != null && !accountNumber.isEmpty()) ? accountNumber
                    : "1028549215";
            String finalAccountName = (accountName != null && !accountName.isEmpty()) ? accountName : "DOAN THE PHONG";
            String finalAmount = (amount != null && !amount.isEmpty()) ? amount : "50000";
            String finalAddInfo = (addInfo != null && !addInfo.isEmpty()) ? addInfo : "Thanh toan don hang";

            // ✅ SỬA: Đổi template và format URL theo chuẩn VietQR
            String vietQRUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-print.png?amount=%s&addInfo=%s&accountName=%s",
                    finalBankCode,
                    finalAccountNumber,
                    finalAmount,
                    URLEncoder.encode(finalAddInfo, StandardCharsets.UTF_8),
                    URLEncoder.encode(finalAccountName, StandardCharsets.UTF_8));

            return new ApiResponse<>(200, "Tạo QR code thành công", vietQRUrl);

        } catch (Exception e) {
            return new ApiResponse<>(500, "Không thể tạo QR code: " + e.getMessage(), null);
        }
    }
}
