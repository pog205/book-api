package org.datn.bookstation.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import org.datn.bookstation.entity.enums.VoucherCategory;
import org.datn.bookstation.entity.enums.DiscountType;

@Data
@Getter
@Setter
public class VoucherRepuest {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private VoucherCategory voucherCategory;
    private DiscountType discountType;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private Long startTime;
    private Long endTime;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
    private String createdBy;
    private String updatedBy;
    
}