package org.datn.bookstation.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleInfoResponse {
    private Long flashSaleItemId;
    private Long flashSaleId;
    private String flashSaleName;
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
    private BigDecimal discountAmount;
    private Double discountPercentage;
    private Integer stockQuantity;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long remainingSeconds;
    private Boolean isActive;
    private String status;
}
