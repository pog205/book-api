package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.VoucherCategory;
import org.datn.bookstation.entity.enums.DiscountType;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

import org.datn.bookstation.entity.enums.VoucherCategory;

@Getter
@Setter
@Entity
@Table(name = "voucher")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "code", nullable = false, length = 20)
    private String code;

    @Size(max = 255)
    @Nationalized
    @Column(name = "name", length = 255)
    private String name;
    
    @Nationalized
    @Column(name = "description")
    private String description;

    //  NEW VOUCHER SYSTEM: Split VoucherType into VoucherCategory + DiscountType
    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_category", nullable = false, length = 20)
    private VoucherCategory voucherCategory = VoucherCategory.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType = DiscountType.PERCENTAGE;

    // Giảm giá theo phần trăm (cho PERCENTAGE discount type)
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    // Giảm giá cố định (cho FIXED_AMOUNT discount type)
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private Long endTime;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "max_discount_value", precision = 10, scale = 2)
    private BigDecimal maxDiscountValue;

    // Số lượng voucher có thể sử dụng
    @Column(name = "usage_limit")
    private Integer usageLimit;

    // Số lượng đã sử dụng
    @ColumnDefault("0")
    @Column(name = "used_count")
    private Integer usedCount = 0;

    // Giới hạn sử dụng trên 1 user
    @ColumnDefault("1")
    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser = 1;

    @ColumnDefault("1")
    @Column(name = "status")
    private Byte status;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

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