package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response cho bán hàng tại quầy
 */
@Getter
@Setter
public class CounterSaleResponse {
    
    private Integer orderId;
    private String orderCode;
    private String orderStatus;
    private String orderType = "COUNTER";
    
    // Thông tin khách hàng
    private Integer userId; // null nếu khách vãng lai
    private String customerName;
    private String customerPhone;
    
    // Thông tin tài chính
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    
    // Chi tiết sản phẩm
    private List<CounterSaleItemResponse> items;
    
    // Voucher đã áp dụng
    private List<VoucherAppliedResponse> appliedVouchers;
    
    // Thông tin giao dịch
    private Integer staffId;
    private String staffName;
    private Long orderDate;
    private String notes;
    
    @Getter
    @Setter
    public static class CounterSaleItemResponse {
        private Integer bookId;
        private String bookName;
        private String bookCode;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private boolean isFlashSale;
        private Integer flashSaleItemId;
        private BigDecimal originalPrice;
        private BigDecimal savedAmount;
    }
    
    @Getter
    @Setter
    public static class VoucherAppliedResponse {
        private Integer voucherId;
        private String voucherCode;
        private String voucherName;
        private BigDecimal discountAmount;
        private String voucherType;
    }
}
