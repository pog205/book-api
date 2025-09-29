package org.datn.bookstation.utils;

import org.datn.bookstation.dto.response.OrderResponse;
import org.datn.bookstation.entity.enums.OrderStatus;

import java.util.*;

/**
 * Utility class xử lý logic trạng thái đơn hàng
 * Bao gồm hiển thị tiếng Việt và trạng thái có thể chuyển
 */
public class OrderStatusUtil {
    
    /**
     * Map hiển thị trạng thái tiếng Việt
     */
    private static final Map<OrderStatus, String> STATUS_DISPLAY_MAP;
    
    static {
        Map<OrderStatus, String> displayMap = new HashMap<>();
        displayMap.put(OrderStatus.PENDING, "Chờ xử lý");
        displayMap.put(OrderStatus.CONFIRMED, "Đã xác nhận");
        displayMap.put(OrderStatus.SHIPPED, "Đang giao hàng");
        displayMap.put(OrderStatus.DELIVERED, "Đã giao hàng thành công");
        displayMap.put(OrderStatus.DELIVERY_FAILED, "Giao hàng thất bại");
        displayMap.put(OrderStatus.REDELIVERING, "Đang giao lại");
        displayMap.put(OrderStatus.RETURNING_TO_WAREHOUSE, "Đang trả về kho");
        displayMap.put(OrderStatus.CANCELED, "Đã hủy");
        
        // ✅ LUỒNG HOÀN TRẢ THỰC TẾ
        displayMap.put(OrderStatus.REFUND_REQUESTED, "Yêu cầu hoàn trả");
        displayMap.put(OrderStatus.AWAITING_GOODS_RETURN, "Chờ lấy hàng hoàn trả");
        displayMap.put(OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER, "Đã nhận hàng từ khách");
        displayMap.put(OrderStatus.GOODS_RETURNED_TO_WAREHOUSE, "Hàng đã về kho");
        displayMap.put(OrderStatus.REFUNDING, "Đang hoàn tiền");
        displayMap.put(OrderStatus.REFUNDED, "Đã hoàn tiền hoàn tất");
        displayMap.put(OrderStatus.PARTIALLY_REFUNDED, "Đã hoàn tiền một phần");
        
        STATUS_DISPLAY_MAP = Collections.unmodifiableMap(displayMap);
    }
    
    /**
     * Luồng chuyển đổi trạng thái hợp lệ
     */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS;
    
    static {
        Map<OrderStatus, Set<OrderStatus>> transitions = new HashMap<>();
        
        // Luồng chuyển trạng thái theo thực tế
        transitions.put(OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELED));
        transitions.put(OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELED));
        transitions.put(OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED, OrderStatus.DELIVERY_FAILED));
        
        // ✅ SỬA: DELIVERED không có trạng thái tiếp theo vì REFUND_REQUESTED chỉ do USER tạo
        transitions.put(OrderStatus.DELIVERED, Set.of()); // Không có chuyển trạng thái nào từ DELIVERED
        
        // Luồng giao hàng thất bại
        transitions.put(OrderStatus.DELIVERY_FAILED, Set.of(OrderStatus.REDELIVERING, OrderStatus.RETURNING_TO_WAREHOUSE));
        transitions.put(OrderStatus.REDELIVERING, Set.of(OrderStatus.DELIVERED, OrderStatus.RETURNING_TO_WAREHOUSE));
        transitions.put(OrderStatus.RETURNING_TO_WAREHOUSE, Set.of(OrderStatus.GOODS_RETURNED_TO_WAREHOUSE));
        
        // ✅ LUỒNG HOÀN TRẢ THỰC TẾ - CHỈ ADMIN ĐƯỢC CHUYỂN
        // ❌ REFUND_REQUESTED không có trạng thái tiếp theo - chỉ admin approve/reject qua API riêng
        transitions.put(OrderStatus.REFUND_REQUESTED, Set.of()); // Không có chuyển trạng thái thủ công
        
        // Chờ lấy hàng hoàn trả → Admin xác nhận đã nhận hàng từ khách
        transitions.put(OrderStatus.AWAITING_GOODS_RETURN, Set.of(OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER));
        
        // Đã nhận hàng từ khách → Hàng về kho (admin xác nhận hàng đã về kho)
        transitions.put(OrderStatus.GOODS_RECEIVED_FROM_CUSTOMER, Set.of(OrderStatus.GOODS_RETURNED_TO_WAREHOUSE));
        
        // ✅ CHỈ KHI HÀNG ĐÃ VỀ KHO MỚI ĐƯỢC HOÀN TIỀN
        // Hàng đã về kho → Có thể hoàn tiền (thông qua API processRefund)
        transitions.put(OrderStatus.GOODS_RETURNED_TO_WAREHOUSE, Set.of()); // Không có chuyển thủ công
        
        // ✅ PARTIALLY_REFUNDED không có trạng thái tiếp theo - user tạo yêu cầu hoàn mới qua API
        transitions.put(OrderStatus.PARTIALLY_REFUNDED, Set.of()); // User tạo refund request mới
        
        // Trạng thái cuối
        transitions.put(OrderStatus.CANCELED, Set.of());
        transitions.put(OrderStatus.REFUNDED, Set.of());
        
        VALID_TRANSITIONS = Collections.unmodifiableMap(transitions);
    }
    
    /**
     * Lấy tên hiển thị tiếng Việt của trạng thái
     */
    public static String getStatusDisplayName(OrderStatus status) {
        if (status == null) return "";
        
        String displayName = STATUS_DISPLAY_MAP.get(status);
        return displayName != null ? displayName : status.name(); // Fallback
    }
    
    /**
     * Lấy danh sách trạng thái có thể chuyển
     */
    public static List<OrderResponse.StatusTransitionOption> getAvailableTransitions(OrderStatus currentStatus) {
        Set<OrderStatus> availableStatuses = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        
        return availableStatuses.stream()
            .map(targetStatus -> {
                OrderResponse.StatusTransitionOption option = new OrderResponse.StatusTransitionOption();
                option.setTargetStatus(targetStatus);
                option.setDisplayName(getStatusDisplayName(targetStatus));
                option.setActionDescription(getActionDescription(currentStatus, targetStatus));
                option.setRequiresConfirmation(requiresConfirmation(targetStatus));
                option.setBusinessImpactNote(getBusinessImpactNote(targetStatus));
                return option;
            })
            .sorted(Comparator.comparing(option -> option.getTargetStatus().ordinal()))
            .toList();
    }
    
    /**
     * Lấy mô tả hành động chuyển đổi
     */
    private static String getActionDescription(OrderStatus currentStatus, OrderStatus targetStatus) {
        return switch (targetStatus) {
            case CONFIRMED -> "Xác nhận đơn hàng";
            case CANCELED -> "Hủy đơn hàng";
            case SHIPPED -> "Bắt đầu giao hàng";
            case DELIVERED -> "Đánh dấu giao hàng thành công";
            case DELIVERY_FAILED -> "Đánh dấu giao hàng thất bại";
            case REDELIVERING -> "Tiến hành giao lại";
            case RETURNING_TO_WAREHOUSE -> "Trả hàng về kho";
            case GOODS_RETURNED_TO_WAREHOUSE -> "Hoàn tất trả hàng về kho";
            case REFUND_REQUESTED -> "Khách yêu cầu hoàn trả";
            case REFUNDING -> "Chấp nhận yêu cầu hoàn trả";
            case GOODS_RECEIVED_FROM_CUSTOMER -> "Nhận hàng hoàn trả từ khách";
            case REFUNDED -> "Hoàn tiền cho khách hàng";
            default -> "Chuyển sang " + getStatusDisplayName(targetStatus);
        };
    }
    
    /**
     * Kiểm tra có yêu cầu xác nhận đặc biệt không
     */
    private static Boolean requiresConfirmation(OrderStatus targetStatus) {
        return switch (targetStatus) {
            case CANCELED, DELIVERY_FAILED, RETURNING_TO_WAREHOUSE, REFUNDED -> true;
            default -> false;
        };
    }
    
    /**
     * Lấy ghi chú về tác động nghiệp vụ
     */
    private static String getBusinessImpactNote(OrderStatus targetStatus) {
        return switch (targetStatus) {
            case CANCELED -> "Hủy đơn hàng sẽ hoàn lại số lượng sách vào kho";
            case DELIVERY_FAILED -> "Đơn hàng có thể được giao lại hoặc trả về kho";
            case RETURNING_TO_WAREHOUSE -> "Hàng về kho, có thể cần hoàn tiền nếu đã thanh toán online";
            case GOODS_RETURNED_TO_WAREHOUSE -> "Số lượng sách sẽ được cộng lại vào kho";
            case REFUNDED -> "Hoàn tiền về tài khoản của khách";
            case REFUNDING -> "Bắt đầu quy trình hoàn trả hàng và hoàn tiền";
            default -> null;
        };
    }
    
    /**
     * Kiểm tra luồng chuyển trạng thái có hợp lệ không
     */
    public static boolean isValidTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        Set<OrderStatus> validNextStatuses = VALID_TRANSITIONS.get(currentStatus);
        return validNextStatuses != null && validNextStatuses.contains(newStatus);
    }
}
