package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookStatsOverviewResponse {
    
    // � THỐNG KÊ SÁCH CƠ BẢN - CHỈ LIÊN QUAN ĐẾN SÁCH
    private Long totalBooks;                    // Tổng số sách trong hệ thống
    private Long totalBooksInStock;             // Tổng số sách còn trong kho
    private Long totalOutOfStock;               // Số sách hết hàng
    
    // 🎯 THỐNG KÊ KHUYẾN MÃI SÁCH
    private Long totalBooksWithDiscount;        // Số sách đang giảm giá
    private Long totalBooksInFlashSale;         // Số sách trong flash sale
}
