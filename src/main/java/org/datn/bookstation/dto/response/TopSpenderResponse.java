package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopSpenderResponse {
    private String fullName;
    private BigDecimal totalSpent;
    private String rankName;
}