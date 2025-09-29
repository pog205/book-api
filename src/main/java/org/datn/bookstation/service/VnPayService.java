package org.datn.bookstation.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.datn.bookstation.configuration.VnPayProperties;
import org.datn.bookstation.entity.Order;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String HMAC_SHA512 = "HmacSHA512";

    private final VnPayProperties properties;

    /**
     * Sinh URL thanh toán cho order để frontend redirect.
     *
     * @param order    Đơn hàng cần thanh toán (đã có totalAmount và code)
     * @param clientIp Địa chỉ IP của client (lấy từ HttpServletRequest)
     * @return URL hoàn chỉnh để redirect sang VNPAY
     */
    public String generatePaymentUrl(Order order, String clientIp) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", properties.getTmnCode());

        // Số tiền nhân 100 theo quy định VNPAY
        long amount = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        vnpParams.put("vnp_Amount", String.valueOf(amount));

        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", order.getCode());
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang:" + order.getCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", properties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", clientIp);
        vnpParams.put("vnp_CreateDate", LocalDateTime.now().format(VNPAY_DATE_FORMAT));

        // Bước 1: sort param
        String queryString = buildQueryString(vnpParams);

        // Bước 2: tạo chữ ký
        String secureHash = hmacSHA512(properties.getHashSecret(), queryString);

        return properties.getPayUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Sinh URL thanh toán cho mục đích test thủ công, không phụ thuộc Order.
     * @param amountVnd   Số tiền VND, ví dụ 100000 (đã là VND, chưa nhân 100)
     * @param orderCode   Mã đơn (hoặc mã tham chiếu tuỳ ý)
     * @param orderInfo   Thông tin mô tả đơn hàng
     * @param clientIp    IP client
     */
    public String generatePaymentUrl(BigDecimal amountVnd, String orderCode, String orderInfo, String clientIp) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", properties.getTmnCode());

        long amount = amountVnd.multiply(BigDecimal.valueOf(100)).longValue();
        vnpParams.put("vnp_Amount", String.valueOf(amount));

        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderCode);
        vnpParams.put("vnp_OrderInfo", orderInfo != null ? orderInfo : ("Thanh toan don hang:" + orderCode));
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", properties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", clientIp);
        vnpParams.put("vnp_CreateDate", LocalDateTime.now().format(VNPAY_DATE_FORMAT));

        String queryString = buildQueryString(vnpParams);
        String secureHash = hmacSHA512(properties.getHashSecret(), queryString);
        return properties.getPayUrl() + "?" + queryString + "&vnp_SecureHash=" + secureHash;
    }

    /**
     * Xác thực checksum của VNPAY gửi về.
     */
    public boolean validateChecksum(Map<String, String> vnpParams, String receivedHash) {
        // Xóa các field không dùng khi tính hash
        vnpParams.remove("vnp_SecureHashType");
        vnpParams.remove("vnp_SecureHash");
        String queryString = buildQueryString(vnpParams);
        String calculatedHash = hmacSHA512(properties.getHashSecret(), queryString);
        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    /**
     * Build query string theo thứ tự alphabet và mã hóa URL value
     */
    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    /**
     * Tính HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            hmac.init(secretKeySpec);
            byte[] result = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(result);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo chữ ký hash", e);
        }
    }
} 