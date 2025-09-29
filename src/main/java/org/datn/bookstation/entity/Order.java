package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import org.datn.bookstation.entity.enums.OrderStatus;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "\"order\"")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private User staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    //  THÊM: Thông tin người nhận cho đơn hàng tại quầy (khi address_id null)
    @Size(max = 100)
    @Nationalized
    @Column(name = "recipient_name", length = 100)
    private String recipientName;

    @Size(max = 20)
    @Nationalized
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotNull
    @Column(name = "order_date", nullable = false)
    private Long orderDate;

    // Tổng tiền sản phẩm (chưa tính phí ship, chưa giảm giá)
    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 20, scale = 2)
    private BigDecimal subtotal;

    // Phí vận chuyển
    @ColumnDefault("0")
    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    // Tổng giảm giá từ voucher sản phẩm
    @ColumnDefault("0")
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Giảm giá phí ship
    @ColumnDefault("0")
    @Column(name = "discount_shipping", precision = 10, scale = 2)
    private BigDecimal discountShipping = BigDecimal.ZERO;

    // Tổng tiền cuối cùng khách phải trả
    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 20, scale = 2)
    private BigDecimal totalAmount;

    @ColumnDefault("1")
    @Column(name = "status")
    private Byte status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", length = 30)  //  Tăng từ 20 lên 30 để chứa GOODS_RETURNED_TO_WAREHOUSE
    private OrderStatus orderStatus;
    
    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;
    
    //  THÊM MỚI: Phương thức thanh toán
    @Size(max = 20)
    @Nationalized
    @Column(name = "payment_method", length = 20)
    private String paymentMethod; // "COD", "ONLINE_PAYMENT", "BANK_TRANSFER", etc.

    @Nationalized
    @Lob
    @Column(name = "notes")
    private String notes;

    // Lý do hủy/hoàn trả
    @Nationalized
    @Lob
    @Column(name = "cancel_reason")
    private String cancelReason;

    // Số lượng voucher thường đã áp dụng (tối đa 1)
    @ColumnDefault("0")
    @Column(name = "regular_voucher_count")
    private Integer regularVoucherCount = 0;

    // Số lượng voucher freeship đã áp dụng (tối đa 1)  
    @ColumnDefault("0")
    @Column(name = "shipping_voucher_count")
    private Integer shippingVoucherCount = 0;

    @Column(name = "created_at", nullable = false)
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private java.util.List<OrderDetail> orderDetails;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "order_voucher",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "voucher_id")
    )
    private java.util.List<Voucher> vouchers;

    public java.util.List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public java.util.List<Voucher> getVouchers() {
        return vouchers;
    }
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}