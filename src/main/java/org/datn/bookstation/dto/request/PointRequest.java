package org.datn.bookstation.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PointRequest {
    private String email;
    private Integer orderId;
    private Integer pointEarned;
    private BigDecimal minSpent;
    private Integer pointSpent;
    private String description;
    private Byte status;
}
