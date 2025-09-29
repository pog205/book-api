package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 📊 Book Statistics Response - Đơn giản hóa theo yêu cầu mới
 * Chỉ trả về list sách với thông tin cơ bản + doanh thu + tăng trưởng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatsResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookStats {
        // Thông tin cơ bản từng sách
        private String code;              // Mã sách
        private String name;              // Tên sách
        private String isbn;              // ISBN
        private BigDecimal currentPrice;  // Giá đang bán

        // Doanh thu
        private BigDecimal revenue;              // Doanh thu hiện tại
        private Double revenueGrowthPercent;     // % tăng/giảm doanh thu so với kỳ trước
        private BigDecimal revenueGrowthValue;   // Giá trị tăng/giảm doanh thu

        // Số lượng bán
        private Integer quantitySold;            // Số lượng bán hiện tại
        private Double quantityGrowthPercent;    // % tăng/giảm số lượng so với kỳ trước
        private Integer quantityGrowthValue;     // Số lượng tăng/giảm tuyệt đối
    }
    
    @Builder.Default
    private String status = "success";
    private String message;
    private List<BookStats> data;
}
