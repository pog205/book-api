package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointResponse {
    private Integer id;
    private String email;
    private String orderCode;
    private Integer pointEarned;
    private BigDecimal minSpent;
    private Integer pointSpent;
    private String description;
    private Long createdAt;     // Thêm trường ngày tạo
    private Long updatedAt;     // Thêm trường ngày cập nhật
}
