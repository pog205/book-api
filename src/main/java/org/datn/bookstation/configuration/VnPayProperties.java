package org.datn.bookstation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vnpay")
public class VnPayProperties {
    /** Mã định danh TMNCode do VNPAY cấp */
    private String tmnCode;

    /** Chuỗi bí mật dùng để tạo checksum */
    private String hashSecret;

    /** URL cổng thanh toán (sandbox / prod) */
    private String payUrl;

    /** URL VNPAY redirect người dùng về sau khi thanh toán */
    private String returnUrl;

    /** URL VNPAY gọi server-to-server để thông báo kết quả */
    private String ipnUrl;
} 