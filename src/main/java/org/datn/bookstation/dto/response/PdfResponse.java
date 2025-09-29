package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfResponse {
    // Thông tin đơn hàng
    private String orderCode;
    private Date orderDate;
    private String orderType;
    private String paymentMethod;
    private String orderStatus;
    private String notes;

    // Thông tin khách hàng
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String deliveryAddress;

    // Thông tin nhân viên (nếu có)
    private String staffName;

    // Chi tiết sản phẩm
    private List<OrderItemDto> orderItems;

    // Thông tin tài chính
    private BigDecimal subtotal; // Tạm tính (chưa giảm giá, chưa phí ship)
    private BigDecimal totalDiscountAmount; // Tổng giảm giá sản phẩm
    private BigDecimal shippingFee; // Phí vận chuyển
    private BigDecimal shippingDiscount; // Giảm giá phí ship
    private BigDecimal totalAmount; // Tổng cộng cuối cùng

    // Voucher đã áp dụng
    private List<AppliedVoucherDto> appliedVouchers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemDto {
        private String bookName;
        private String bookCode;
        private Integer quantity;
        private BigDecimal originalPrice; // Giá gốc của sách
        private BigDecimal unitPrice; // Giá bán thực tế (sau discount sách/flash sale)
        private BigDecimal itemDiscountAmount; // Giảm giá riêng của sách
        private BigDecimal voucherDiscountAmount; // Giảm giá từ voucher
        private BigDecimal totalAmount; // Thành tiền = quantity * unitPrice - voucherDiscountAmount
        private BigDecimal flashSalePrice; // Giá flash sale (nếu có)
        private Boolean isFlashSale; // Có phải flash sale không
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AppliedVoucherDto {
        private String code;
        private String name;
        private String type; // "NORMAL" hoặc "FREESHIP"
        private BigDecimal discountAmount; // Số tiền đã giảm
    }
}
