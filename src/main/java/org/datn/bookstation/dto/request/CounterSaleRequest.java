package org.datn.bookstation.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho đặt hàng tại quầy (Counter Sales)
 * Khác với OrderRequest ở chỗ:
 * - Không cần userId (có thể bán cho khách vãng lai)
 * - Không cần addressId (bán tại quầy)
 * - Chỉ cần số điện thoại và tên khách hàng
 */
@Getter
@Setter
public class CounterSaleRequest {
    
    // Thông tin khách hàng (optional - có thể bán cho khách vãng lai)
    private Integer userId; // Optional: nếu khách hàng có tài khoản
    
    @Size(max = 100, message = "Tên khách hàng không được vượt quá 100 ký tự")
    private String customerName; // Optional: tên khách hàng vãng lai
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 chữ số")
    private String customerPhone; // Optional: số điện thoại khách hàng vãng lai
    
    // Chi tiết sản phẩm
    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    private List<OrderDetailRequest> orderDetails;
    
    // Voucher áp dụng
    private List<Integer> voucherIds;
    
    // Thông tin tài chính
    @NotNull(message = "Subtotal không được để trống")
    private BigDecimal subtotal;
    
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount;
    
    // Thông tin bổ sung
    private String notes;
    
    // Staff thực hiện giao dịch
    @NotNull(message = "Staff ID không được để trống")
    private Integer staffId;
    
    // Phương thức thanh toán
    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Pattern(regexp = "CASH|CARD|BANK_TRANSFER", message = "Phương thức thanh toán không hợp lệ")
    private String paymentMethod = "CASH"; // Default: tiền mặt
}
