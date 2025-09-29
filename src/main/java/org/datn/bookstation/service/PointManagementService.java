package org.datn.bookstation.service;

import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.User;

import java.math.BigDecimal;

/**
 * Service quản lý tích điểm và xử lý rank của người dùng
 */
public interface PointManagementService {
    
    /**
     * Tích điểm cho đơn hàng đã hoàn thành
     * @param order Đơn hàng
     * @param user Người dùng
     */
    void earnPointsFromOrder(Order order, User user);
    
    /**
     * Trừ điểm khi hủy đơn hàng đã tích điểm
     * @param order Đơn hàng bị hủy
     * @param user Người dùng
     */
    void deductPointsFromCancelledOrder(Order order, User user);
    
    /**
     * Hoàn lại điểm khi đơn hàng bị trả
     * @param order Đơn hàng bị trả
     * @param user Người dùng
     */
    void refundPointsFromReturnedOrder(Order order, User user);
    
    /**
     * ✅ THÊM MỚI: Trừ điểm cho hoàn trả một phần
     * @param refundAmount Số tiền hoàn trả
     * @param order Đơn hàng gốc
     * @param user Người dùng
     */
    void deductPointsFromPartialRefund(BigDecimal refundAmount, Order order, User user);
    
    /**
     * Tính số điểm được tích dựa trên số tiền đã chi và rank của user
     * @param totalAmount Tổng tiền đã chi
     * @param user Người dùng (để lấy rank)
     * @return Số điểm được tích
     */
    int calculateEarnedPoints(BigDecimal totalAmount, User user);
    
    /**
     * Cập nhật rank cho user dựa trên tổng chi tiêu
     * @param user Người dùng
     */
    void updateUserRank(User user);
    
    /**
     * Kiểm tra và cập nhật rank tự động cho user
     * @param userId ID người dùng
     */
    void checkAndUpdateUserRank(Integer userId);
}
