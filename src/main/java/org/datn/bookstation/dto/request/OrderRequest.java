package org.datn.bookstation.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    
    // ✅ MODIFIED: userId có thể null cho counter sales (khách vãng lai)
    private Integer userId; // Required for online orders, optional for counter sales
    
    private Integer staffId; // Optional - for staff processing
    
    // ✅ MODIFIED: addressId có thể null cho counter sales (không cần giao hàng)
    private Integer addressId; // Required for online orders, null for counter sales
    
    // ✅ THÊM: Thông tin người nhận cho đơn hàng tại quầy (khi addressId null)
    private String recipientName; // For counter sales when userId/addressId is null
    private String phoneNumber; // For counter sales when userId/addressId is null
    
    @NotNull(message = "Phí vận chuyển không được để trống")
    private BigDecimal shippingFee = BigDecimal.ZERO;
    
    // Note: subtotal sẽ được tính tự động từ orderDetails
    // discountAmount và discountShipping sẽ được tính từ vouchers
    // totalAmount sẽ được tính: subtotal + shippingFee - discountAmount - discountShipping
    
    private OrderStatus orderStatus = OrderStatus.PENDING;
    
    @NotNull(message = "Loại đơn hàng không được để trống")
    private String orderType;
    
    private String paymentMethod; // ✅ THÊM MỚI: "COD", "ONLINE_PAYMENT", etc.
    
    @NotEmpty(message = "Chi tiết đơn hàng không được để trống")
    private List<OrderDetailRequest> orderDetails;
    
    private List<Integer> voucherIds; // Optional vouchers applied
    
    private String notes; // Optional order notes

    // ✅ BACKEND TỰ TÍNH TOÁN - Frontend không cần truyền
    // subtotal và totalAmount sẽ được backend tự tính dựa trên orderDetails và vouchers
    private BigDecimal subtotal; // Optional - backend sẽ tự tính từ orderDetails
    private BigDecimal totalAmount; // Optional - backend sẽ tự tính: subtotal + shipping - discounts
}
