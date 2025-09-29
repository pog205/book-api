package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherStatsResponse {
    private Long totalVouchers; // Tổng số voucher
    private Long activeVouchers; // Voucher đang hoạt động
    private Long totalUsageCount; // Lượt sử dụng voucher
    private String mostPopularVoucher; // Voucher phổ biến nhất
}