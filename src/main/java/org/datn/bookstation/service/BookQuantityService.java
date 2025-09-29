package org.datn.bookstation.service;

import org.datn.bookstation.entity.OrderDetail;
import org.datn.bookstation.entity.enums.OrderStatus;

import java.util.List;

/**
 * Service quản lý số lượng sách (processing quantity) khi đặt hàng và chuyển trạng thái
 */
public interface BookQuantityService {
    
    /**
     * Cộng số lượng đang xử lý khi tạo đơn hàng
     */
    void increaseProcessingQuantity(List<OrderDetail> orderDetails);
    
    /**
     * Trừ số lượng đang xử lý và cộng số lượng đã bán khi giao hàng thành công
     */
    void moveProcessingToSold(List<OrderDetail> orderDetails);
    
    /**
     * Trừ số lượng đang xử lý khi hủy đơn hàng
     */
    void decreaseProcessingQuantity(List<OrderDetail> orderDetails);
    
    /**
     * Xử lý thay đổi số lượng khi chuyển trạng thái đơn hàng
     */
    void handleOrderStatusChange(Integer orderId, OrderStatus oldStatus, OrderStatus newStatus);
    
    /**
     * Xử lý số lượng khi hoàn hàng
     */
    void handleRefund(List<OrderDetail> orderDetails, boolean isPartialRefund);
}
