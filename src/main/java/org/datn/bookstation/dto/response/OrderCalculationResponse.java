package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCalculationResponse {
    
    private Integer userId;
    private String userEmail;
    private String userName;
    
    private List<ItemCalculationDetail> itemDetails;
    
    private BigDecimal subtotal; // Tổng tiền hàng (đã áp dụng flash sale)
    private BigDecimal shippingFee; // Phí vận chuyển
    private BigDecimal totalBeforeDiscount; // Tổng trước khi giảm giá voucher
    
    // Voucher breakdown
    private BigDecimal regularVoucherDiscount; // Giảm giá voucher thường
    private BigDecimal shippingVoucherDiscount; // Giảm giá voucher ship
    private BigDecimal totalVoucherDiscount; // Tổng giảm giá voucher
    private List<VoucherDetail> appliedVouchers;
    
    private BigDecimal finalTotal; // Tổng cuối cùng phải trả
    
    private String message; // Thông báo cho admin
    
    @Getter
    @Setter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemCalculationDetail {
        private Integer bookId;
        private String bookName;
        private String bookCode;
        private Integer quantity;
        private BigDecimal originalPrice; // Giá gốc
        private BigDecimal unitPrice; // Giá đã áp dụng flash sale (nếu có)
        private BigDecimal itemTotal; // Tổng tiền item (quantity * unitPrice)
        private Boolean isFlashSale; // Có áp dụng flash sale không
        private Integer flashSaleItemId; // ID flash sale item (nếu có)
        private BigDecimal savedAmount; // Số tiền tiết kiệm được từ flash sale
        private String flashSaleName; // Tên flash sale
    }
    
    @Getter
    @Setter
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VoucherDetail {
        private Integer voucherId;
        private String voucherCode;
        private String voucherName;
        private String voucherType; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
        private BigDecimal discountApplied; // Số tiền được giảm
        private String description; // Mô tả voucher
    }
}
