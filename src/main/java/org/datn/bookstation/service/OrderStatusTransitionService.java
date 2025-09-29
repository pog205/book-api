package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.OrderStatusTransitionRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.OrderStatusTransitionResponse;
import org.datn.bookstation.entity.enums.OrderStatus;

import java.util.List;

/**
 * Service chuyên xử lý chuyển đổi trạng thái đơn hàng với đầy đủ logic nghiệp vụ
 * 
 * Các quy tắc chuyển đổi trạng thái:
 * PENDING → CONFIRMED → SHIPPED → DELIVERED
 * PENDING/CONFIRMED → CANCELED
 * DELIVERED → GOODS_RETURNED_TO_WAREHOUSE → REFUNDED
 * DELIVERED → PARTIALLY_REFUNDED
 * CANCELED/RETURNED → REFUNDING → REFUNDED
 */
public interface OrderStatusTransitionService {
    
    /**
     * Thực hiện chuyển đổi trạng thái đơn hàng với đầy đủ validation và business logic
     * 
     * @param request Thông tin yêu cầu chuyển đổi trạng thái
     * @return Kết quả chuyển đổi với thông tin tác động nghiệp vụ
     */
    ApiResponse<OrderStatusTransitionResponse> transitionOrderStatus(OrderStatusTransitionRequest request);
    
    /**
     * Kiểm tra xem có thể chuyển từ trạng thái hiện tại sang trạng thái mới hay không
     * 
     * @param currentStatus Trạng thái hiện tại
     * @param newStatus Trạng thái mới
     * @return true nếu có thể chuyển đổi
     */
    boolean isValidTransition(OrderStatus currentStatus, OrderStatus newStatus);
    
    /**
     * Lấy danh sách các trạng thái có thể chuyển đổi từ trạng thái hiện tại
     * 
     * @param currentStatus Trạng thái hiện tại
     * @return Danh sách trạng thái có thể chuyển đổi
     */
    List<OrderStatus> getValidNextStatuses(OrderStatus currentStatus);
    
    /**
     * Lấy thông tin mô tả về việc chuyển đổi trạng thái
     * 
     * @param currentStatus Trạng thái hiện tại
     * @param newStatus Trạng thái mới
     * @return Mô tả bằng tiếng Việt về việc chuyển đổi
     */
    String getTransitionDescription(OrderStatus currentStatus, OrderStatus newStatus);
}
