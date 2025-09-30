package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "checkout_session")
public class CheckoutSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Địa chỉ giao hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    // Phương thức vận chuyển
    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;

    // Tiền ship
    @ColumnDefault("0")
    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    // Dự kiến giao từ khi nào (timestamp milliseconds)
    @Column(name = "estimated_delivery_from")
    private Long estimatedDeliveryFrom;

    // Dự kiến giao đến khi nào (timestamp milliseconds)
    @Column(name = "estimated_delivery_to")
    private Long estimatedDeliveryTo;

    // Phương thức thanh toán
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    // ID voucher đã chọn (JSON array)
    @Column(name = "selected_voucher_ids")
    private String selectedVoucherIds; // JSON: [1,2] hoặc null

    // Thông tin sản phẩm (JSON array của checkout items)
    @NotNull
    @Column(name = "checkout_items", nullable = false)
    private String checkoutItems; // JSON: [{"bookId":1,"quantity":2,"isFlashSale":true,"flashSaleItemId":1}]

    // Tổng tiền sản phẩm (chưa tính ship, voucher)
    @ColumnDefault("0")
    @Column(name = "subtotal", precision = 20, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    // Tổng giảm giá voucher
    @ColumnDefault("0")
    @Column(name = "total_discount", precision = 10, scale = 2)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    // Tổng tiền cuối cùng
    @ColumnDefault("0")
    @Column(name = "total_amount", precision = 20, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Trạng thái session
    @ColumnDefault("1")
    @Column(name = "status")
    private Byte status = 1; // 1: active, 0: expired, 2: completed

    // Thời gian hết hạn (milliseconds)
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Long expiresAt;

    // Thông tin audit
    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    // Ghi chú thêm
    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
        
        // Mặc định hết hạn sau 24 giờ
        if (expiresAt == null) {
            expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }

    // Helper methods
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public boolean isActive() {
        return status == 1 && !isExpired();
    }
}
