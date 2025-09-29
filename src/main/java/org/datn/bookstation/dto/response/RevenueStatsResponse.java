package org.datn.bookstation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevenueStatsResponse {
    private Integer year;
    private Integer month; // null nếu không dùng
    private Integer week; // null nếu không dùng
    private BigDecimal revenue;
    private String day; // đổi từ Integer -> String: "yyyy-MM-dd"

    // Constructor cũ để tái sử dụng nơi khác
    public RevenueStatsResponse(Integer year, Integer month, Integer week, BigDecimal revenue) {
        this.year = year;
        this.month = month;
        this.week = week;
        this.revenue = revenue;
    }
}
