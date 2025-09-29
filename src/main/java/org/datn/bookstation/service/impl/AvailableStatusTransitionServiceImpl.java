package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.AvailableStatusTransitionResponse;
import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.OrderRepository;
import org.datn.bookstation.service.AvailableStatusTransitionService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementation của AvailableStatusTransitionService
 * Xử lý logic trả về danh sách trạng thái có thể chuyển theo thực tế nghiệp vụ
 */
@Service
@AllArgsConstructor
@Slf4j
public class AvailableStatusTransitionServiceImpl implements AvailableStatusTransitionService {
    
    private final OrderRepository orderRepository;
    
    /**
     * Định nghĩa các luồng chuyển đổi trạng thái hợp lệ theo thực tế
     * Bao gồm các trường hợp giao hàng thất bại và xử lý hoàn tiền
     */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS;
    
    static {
        Map<OrderStatus, Set<OrderStatus>> transitions = new HashMap<>();
        
        // 1. Chờ xử lý → Đã xác nhận hoặc Đã hủy
        transitions.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELED));
        
        // 2. Đã xác nhận → Đang giao hàng hoặc Đã hủy
        transitions.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELED));
        
        // 3. Đang giao hàng → Giao thành công hoặc Giao thất bại
        transitions.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED));
        
        // 4. Giao thành công → Có thể yêu cầu hoàn trả
        transitions.put(OrderStatus.DELIVERED, Set.of(OrderStatus.REFUND_REQUESTED));
        
        // 5. Giao thất bại → Giao lại hoặc Trả về kho (khách không nhận)
        transitions.put(OrderStatus.DELIVERY_FAILED, Set.of(OrderStatus.REDELIVERING, OrderStatus.RETURNING_TO_WAREHOUSE));
        
        // 6. Đang giao lại → Giao thành công hoặc Trả về kho
        transitions.put(OrderStatus.REDELIVERING, Set.of(OrderStatus.DELIVERED, OrderStatus.RETURNING_TO_WAREHOUSE));
        
        // 7. Đang trả về kho → Đã trả về kho
        transitions.put(OrderStatus.RETURNING_TO_WAREHOUSE, Set.of(OrderStatus.GOODS_RETURNED_TO_WAREHOUSE));
        
        // 8. Đã hủy → Không thể chuyển đi đâu (trạng thái cuối)
        transitions.put(OrderStatus.CANCELED, Set.of());
        
        // 9. Yêu cầu hoàn trả → Admin xử lý thành Đang hoàn tiền hoặc từ chối
        transitions.put(OrderStatus.REFUND_REQUESTED, Set.of(OrderStatus.REFUNDING, OrderStatus.DELIVERED));
        
        // 10. Đang hoàn tiền → Nhận hàng từ khách
        transitions.put(OrderStatus.REFUNDING, Set.of(OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER));
        
        // 11. Đã nhận hàng từ khách → Hàng về kho
        transitions.put(OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER, Set.of(OrderStatus.GOODS_RETURNED_TO_WAREHOUSE));
        
        // 12. Hàng đã về kho → Hoàn tiền hoàn tất (nếu đã thanh toán online)
        transitions.put(OrderStatus.GOODS_RETURNED_TO_WAREHOUSE, Set.of(OrderStatus.REFUNDED));
        
        // 13. Hoàn tiền hoàn tất → Trạng thái cuối
        transitions.put(OrderStatus.REFUNDED, Set.of());
        
        // 14. Hoàn tiền một phần → Có thể tiếp tục hoàn phần còn lại
        transitions.put(OrderStatus.PARTIALLY_REFUNDED, Set.of(OrderStatus.REFUNDING, OrderStatus.REFUNDED));
        
        VALID_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }
    
    @Override
    public ApiResponse<AvailableStatusTransitionResponse> getAvailableTransitions(Integer orderId) {
        try {
            // 1. Tìm đơn hàng
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));
            
            // 2. Lấy trạng thái hiện tại
            OrderStatus currentStatus = order.getOrderStatus();
            
            // 3. Lấy danh sách trạng thái có thể chuyển
            Set<OrderStatus> availableStatuses = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
            
            // 4. Tạo danh sách options với mô tả chi tiết
            List<AvailableStatusTransitionResponse.StatusTransitionOption> transitionOptions = 
                availableStatuses.stream()
                    .map(targetStatus -> createTransitionOption(currentStatus, targetStatus, order))
                    .sorted(Comparator.comparing(option -> option.getTargetStatus().ordinal()))
                    .toList();
            
            // 5. Tạo response
            AvailableStatusTransitionResponse response = AvailableStatusTransitionResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getCode())
                .currentStatus(currentStatus)
                .currentStatusDisplayName(getStatusDisplayName(currentStatus))
                .availableTransitions(transitionOptions)
                .isPaidOnline(isOrderPaidOnline(order))
                .restrictionNote(getRestrictionNote(currentStatus, order))
                .build();
            
            return new ApiResponse<>(200, "Lấy danh sách trạng thái có thể chuyển thành công", response);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách trạng thái có thể chuyển cho đơn hàng {}: {}", orderId, e.getMessage());
            return new ApiResponse<>(500, "Lỗi hệ thống: " + e.getMessage(), null);
        }
    }
    
    /**
     * Tạo option chuyển trạng thái với mô tả chi tiết
     */
    private AvailableStatusTransitionResponse.StatusTransitionOption createTransitionOption(
            OrderStatus currentStatus, OrderStatus targetStatus, Order order) {
        
        return AvailableStatusTransitionResponse.StatusTransitionOption.builder()
            .targetStatus(targetStatus)
            .displayName(getStatusDisplayName(targetStatus))
            .actionDescription(getActionDescription(currentStatus, targetStatus))
            .requiresConfirmation(requiresConfirmation(currentStatus, targetStatus))
            .businessImpactNote(getBusinessImpactNote(currentStatus, targetStatus, order))
            .build();
    }
    
    /**
     * Lấy tên hiển thị của trạng thái
     */
    private String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ xử lý";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPED -> "Đang giao hàng";
            case DELIVERED -> "Đã giao hàng thành công";
            case DELIVERY_FAILED -> "Giao hàng thất bại";
            case REDELIVERING -> "Đang giao lại";
            case RETURNING_TO_WAREHOUSE -> "Đang trả về kho";
            case CANCELED -> "Đã hủy";
            case REFUND_REQUESTED -> "Yêu cầu hoàn trả";
            case REFUNDING -> "Đang hoàn tiền";
            case GOODS_RECEIVED_FROM_CUSTOMER -> "Đã nhận hàng từ khách";
            case REFUNDED -> "Đã hoàn tiền hoàn tất";
            case GOODS_RETURNED_TO_WAREHOUSE -> "Hàng đã về kho";
            case PARTIALLY_REFUNDED -> "Đã hoàn tiền một phần";
            default -> status.name();
        };
    }
    
    /**
     * Lấy mô tả hành động chuyển đổi
     */
    private String getActionDescription(OrderStatus currentStatus, OrderStatus targetStatus) {
        String key = currentStatus + "_TO_" + targetStatus;
        
        return switch (key) {
            case "PENDING_TO_CONFIRMED" -> "Xác nhận đơn hàng";
            case "PENDING_TO_CANCELED" -> "Hủy đơn hàng";
            case "CONFIRMED_TO_SHIPPED" -> "Bắt đầu giao hàng";
            case "CONFIRMED_TO_CANCELED" -> "Hủy đơn hàng đã xác nhận";
            case "SHIPPED_TO_DELIVERED" -> "Đánh dấu giao hàng thành công";
            case "SHIPPED_TO_DELIVERY_FAILED" -> "Đánh dấu giao hàng thất bại";
            case "DELIVERY_FAILED_TO_REDELIVERING" -> "Tiến hành giao lại";
            case "DELIVERY_FAILED_TO_RETURNING_TO_WAREHOUSE" -> "Trả hàng về kho (khách không nhận)";
            case "REDELIVERING_TO_DELIVERED" -> "Giao lại thành công";
            case "REDELIVERING_TO_RETURNING_TO_WAREHOUSE" -> "Trả hàng về kho (giao lại thất bại)";
            case "RETURNING_TO_WAREHOUSE_TO_GOODS_RETURNED_TO_WAREHOUSE" -> "Hoàn tất trả hàng về kho";
            case "DELIVERED_TO_REFUND_REQUESTED" -> "Khách yêu cầu hoàn trả";
            case "REFUND_REQUESTED_TO_REFUNDING" -> "Chấp nhận yêu cầu hoàn trả";
            case "REFUND_REQUESTED_TO_DELIVERED" -> "Từ chối yêu cầu hoàn trả";
            case "REFUNDING_TO_GOODS_RECEIVED_FROM_CUSTOMER" -> "Nhận hàng hoàn trả từ khách";
            case "GOODS_RECEIVED_FROM_CUSTOMER_TO_GOODS_RETURNED_TO_WAREHOUSE" -> "Nhập hàng hoàn trả về kho";
            case "GOODS_RETURNED_TO_WAREHOUSE_TO_REFUNDED" -> "Hoàn tiền cho khách hàng";
            case "PARTIALLY_REFUNDED_TO_REFUNDING" -> "Tiếp tục hoàn tiền phần còn lại";
            case "PARTIALLY_REFUNDED_TO_REFUNDED" -> "Hoàn tất hoàn tiền";
            default -> "Chuyển từ " + getStatusDisplayName(currentStatus) + " sang " + getStatusDisplayName(targetStatus);
        };
    }
    
    /**
     * Kiểm tra xem có yêu cầu xác nhận đặc biệt không
     */
    private Boolean requiresConfirmation(OrderStatus currentStatus, OrderStatus targetStatus) {
        return switch (targetStatus) {
            case CANCELED, DELIVERY_FAILED, RETURNING_TO_WAREHOUSE, REFUNDED -> true;
            default -> false;
        };
    }
    
    /**
     * Lấy ghi chú về tác động nghiệp vụ
     */
    private String getBusinessImpactNote(OrderStatus currentStatus, OrderStatus targetStatus, Order order) {
        Boolean isPaidOnline = isOrderPaidOnline(order);
        
        return switch (targetStatus) {
            case CANCELED -> "Hủy đơn hàng sẽ hoàn lại số lượng sách vào kho";
            case DELIVERY_FAILED -> "Đơn hàng có thể được giao lại hoặc trả về kho";
            case RETURNING_TO_WAREHOUSE -> isPaidOnline ? 
                "Hàng về kho, cần hoàn tiền cho khách đã thanh toán online" : 
                "Hàng về kho, không cần hoàn tiền (thanh toán COD)";
            case GOODS_RETURNED_TO_WAREHOUSE -> "Số lượng sách sẽ được cộng lại vào kho";
            case REFUNDED -> isPaidOnline ? 
                "Hoàn tiền về tài khoản/ví điện tử của khách" : 
                "Hoàn tiền mặt cho khách";
            case REFUNDING -> "Bắt đầu quy trình hoàn trả hàng và hoàn tiền";
            default -> null;
        };
    }
    
    /**
     * Kiểm tra xem đơn hàng có được thanh toán online không
     * Tạm thời return false vì Order entity chưa có field paymentMethod
     */
    private Boolean isOrderPaidOnline(Order order) {
        // TODO: Cần thêm field paymentMethod vào Order entity
        // Tạm thời kiểm tra qua orderType hoặc notes
        if (order.getOrderType() != null && order.getOrderType().equalsIgnoreCase("ONLINE")) {
            return true; // Đơn online thường là đã thanh toán trước
        }
        return false; // Mặc định là COD
    }
    
    /**
     * Lấy ghi chú hạn chế (nếu có)
     */
    private String getRestrictionNote(OrderStatus currentStatus, Order order) {
        return switch (currentStatus) {
            case CANCELED -> "Đơn hàng đã hủy - không thể thay đổi trạng thái";
            case REFUNDED -> "Đơn hàng đã hoàn tiền hoàn tất - không thể thay đổi trạng thái";
            case DELIVERY_FAILED -> "Đơn giao thất bại - cần quyết định giao lại hay trả về kho";
            default -> null;
        };
    }
}
