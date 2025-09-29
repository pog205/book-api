package org.datn.bookstation.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RankRequest {
    private String rankName;
    private BigDecimal minSpent;
    private BigDecimal pointMultiplier;
    private Byte status;
}
