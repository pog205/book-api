package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.VoucherRepuest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.VoucherResponse;
import org.datn.bookstation.dto.response.VoucherStatsResponse;
import org.datn.bookstation.dto.response.VoucherDropdownResponse;
import java.util.List;

public interface VoucherService {
    java.util.List<org.datn.bookstation.dto.response.AvailableVoucherResponse> getAvailableVouchersForUser(
            Integer userId);

    PaginationResponse<VoucherResponse> getAllWithPagination(
            int page, int size, String code, String name, String voucherCategory, String discountType, Byte status);

    void addVoucher(VoucherRepuest request);

    void editVoucher(VoucherRepuest request);

    void updateStatus(Integer id, byte status, String updatedBy);

    void deleteVoucher(Integer id);

    /**
     *  Search voucher cho counter sales
     * Tìm theo mã voucher hoặc tên voucher
     */
    List<VoucherResponse> searchVouchersForCounterSales(String query, int limit);

    /**
     * Lấy thống kê voucher cho dashboard admin
     */
    VoucherStatsResponse getVoucherStats();

    /**
     * API dropdown voucher cho minigame box system
     * Tìm kiếm voucher theo mã hoặc tên và trả về thông tin đầy đủ
     */
    List<VoucherDropdownResponse> getVoucherDropdown(String search);

    ApiResponse<String> distributeVouchersToSilverRank(Integer voucherId);
    ApiResponse<String> distributeVouchersToGoldRank(Integer voucherId);
    ApiResponse<String> distributeVouchersToDiamondRank(Integer voucherId);
}
