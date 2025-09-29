package org.datn.bookstation.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AvailableVoucherResponse {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String categoryVi;
    private String discountTypeVi;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private Long startTime;
    private Long endTime;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private Integer remainingUses;
    private String expireDate;
    private String discountInfo;
}
