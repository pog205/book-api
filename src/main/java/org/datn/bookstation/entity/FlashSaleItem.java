package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "flash_sale_item")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashSaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    FlashSale flashSale;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @NotNull
    @Column(name = "discount_price", nullable = false, precision = 12, scale = 2)
    BigDecimal discountPrice;

    @NotNull
    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    BigDecimal discountPercentage;

    @NotNull
    @Column(name = "stock_quantity", nullable = false)
    Integer stockQuantity;

    @Column(name = "max_purchase_per_user")
    Integer maxPurchasePerUser;

    // ✅ THÊM MỚI: Số lượng đã bán flash sale
    @ColumnDefault("0")
    @Column(name = "sold_count")
    @Builder.Default
    Integer soldCount = 0;

    @Column(name = "created_at", nullable = false)
    Long createdAt;

    @Column(name = "updated_at")
    Long updatedAt;

    @Column(name = "created_by")
    Long createdBy;

    @Column(name = "updated_by")
    Long updatedBy;

    @ColumnDefault("1")
    @Column(name = "status")
    Byte status;

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