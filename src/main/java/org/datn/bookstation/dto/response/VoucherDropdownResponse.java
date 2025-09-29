package org.datn.bookstation.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.datn.bookstation.entity.enums.VoucherCategory;
import org.datn.bookstation.entity.enums.DiscountType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDropdownResponse {
    
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
    private String createdBy;
    private String updatedBy;
}
