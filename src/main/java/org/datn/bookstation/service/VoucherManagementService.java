package org.datn.bookstation.service;

import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.Voucher;

import java.util.List;

/**
 * Service quản lý voucher và xử lý sử dụng voucher
 */
public interface VoucherManagementService {
    
    /**
     * Sử dụng voucher cho đơn hàng
     * @param order Đơn hàng
     * @param vouchers Danh sách voucher được áp dụng
     */
    void useVouchersForOrder(Order order, List<Voucher> vouchers);
    
    /**
     * Hoàn lại voucher khi hủy đơn hàng
     * @param order Đơn hàng bị hủy
     */
    void refundVouchersFromCancelledOrder(Order order);
    
    /**
     *  THÊM MỚI: Hoàn lại voucher khi trả hàng (RETURN)
     * @param order Đơn hàng bị trả
     */
    void refundVouchersFromReturnedOrder(Order order);
    
    /**
     * Kiểm tra voucher có thể sử dụng không
     * @param voucher Voucher cần kiểm tra
     * @param userId ID người dùng
     * @return Thông báo lỗi (null nếu hợp lệ)
     */
    String validateVoucherUsage(Voucher voucher, Integer userId);
    
    /**
     * Kiểm tra voucher đã hết hạn sử dụng chưa
     * @param voucher Voucher
     * @return true nếu đã hết hạn
     */
    boolean isVoucherExpired(Voucher voucher);
    
    /**
     * Kiểm tra voucher đã hết số lượng chưa
     * @param voucher Voucher
     * @return true nếu đã hết số lượng
     */
    boolean isVoucherOutOfStock(Voucher voucher);
    
    /**
     * Kiểm tra user đã sử dụng voucher vượt quá giới hạn chưa
     * @param voucher Voucher
     * @param userId ID người dùng
     * @return true nếu đã vượt quá giới hạn
     */
    boolean hasUserExceededVoucherLimit(Voucher voucher, Integer userId);
}
