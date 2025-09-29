package org.datn.bookstation.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
public class CheckoutSessionResponse {

    private Integer id;
    private Integer userId;
    private String userFullName;
    private String userEmail;

    // Địa chỉ giao hàng
    private Integer addressId;
    private String addressFullText;
    private String recipientName;
    private String recipientPhone;

    // Thông tin vận chuyển
    private String shippingMethod;
    private BigDecimal shippingFee;
    private Long estimatedDeliveryFrom;
    private Long estimatedDeliveryTo;
    private String estimatedDeliveryText;

    // Thanh toán
    private String paymentMethod;

    // Voucher
    private List<Integer> selectedVoucherIds;
    private List<VoucherSummary> selectedVouchers;

    // Sản phẩm
    private List<CheckoutItemResponse> checkoutItems;

    // Tính toán tiền
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalAmount;

    // Session info
    private Byte status;
    private String statusText;
    private Long expiresAt;
    private Boolean isExpired;
    private Boolean isActive;
    private Long timeRemaining; // milliseconds còn lại

    private Long createdAt;
    private Long updatedAt;
    private String notes;

    @Data
    @Getter
    @Setter
    public static class CheckoutItemResponse {
        private Integer bookId;
        private String bookTitle;
        private String bookImage;
        private String bookAuthor;
        private Integer quantity;
        private Boolean isFlashSale;
        private Integer flashSaleItemId;
        private String flashSaleName;
        private BigDecimal unitPrice;
        private BigDecimal originalPrice;
        private BigDecimal totalPrice;
        private BigDecimal savings; // Tiền tiết kiệm nếu có flash sale
        private Boolean isOutOfStock;
        private Boolean isFlashSaleExpired;
        private Integer availableStock;
    }

    @Data
    @Getter
    @Setter
    public static class VoucherSummary {
    private Integer id;
    private String code;
    private String name;
    private String voucherType;
    private String discountType; // Thêm kiểu giảm giá (PERCENTAGE, FIXED_AMOUNT)
    private BigDecimal discountValue;
    private Boolean isValid;
    private String invalidReason;
    }

    // Helper methods
    public String getStatusText() {
        if (status == null) return "Unknown";
        switch (status) {
            case 1: return isExpired ? "Expired" : "Active";
            case 0: return "Expired";
            case 2: return "Completed";
            default: return "Unknown";
        }
    }

    public Long getTimeRemaining() {
        if (expiresAt == null) return 0L;
        long remaining = expiresAt - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    public Boolean getIsExpired() {
        if (expiresAt == null) return true;
        return System.currentTimeMillis() > expiresAt;
    }

    public Boolean getIsActive() {
        return status != null && status == 1 && !getIsExpired();
    }
}
