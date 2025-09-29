package org.datn.bookstation.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RankResponse {
    private Integer id;
    private String name;
    private BigDecimal minSpent;
    private BigDecimal pointMultiplier;
    private Byte status;
}
