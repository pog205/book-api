package org.datn.bookstation.dto.response;

import java.math.BigDecimal;
import org.datn.bookstation.entity.enums.VoucherCategory;
import lombok.Data;

@Data
public class voucherUserResponse {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private VoucherCategory voucherCategory;
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

    // Constructor cho JPQL
    public voucherUserResponse(Integer id, String code, String name, String description, VoucherCategory voucherCategory,
                               BigDecimal discountPercentage, BigDecimal discountAmount, Long startTime, Long endTime,
                               BigDecimal minOrderValue, BigDecimal maxDiscountValue, Integer usageLimit, Integer usedCount,
                               Integer usageLimitPerUser, Byte status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.voucherCategory = voucherCategory;
        this.discountPercentage = discountPercentage;
        this.discountAmount = discountAmount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minOrderValue = minOrderValue;
        this.maxDiscountValue = maxDiscountValue;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.usageLimitPerUser = usageLimitPerUser;
        this.status = status;
    }
}
