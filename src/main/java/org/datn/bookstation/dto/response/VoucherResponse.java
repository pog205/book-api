package org.datn.bookstation.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import org.datn.bookstation.entity.enums.VoucherCategory;
import org.datn.bookstation.entity.enums.DiscountType;

@Getter
@Setter
public class VoucherResponse {
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
