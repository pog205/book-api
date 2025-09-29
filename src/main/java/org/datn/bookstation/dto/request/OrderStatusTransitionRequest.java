package org.datn.bookstation.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.datn.bookstation.entity.enums.OrderStatus;

import jakarta.validation.constraints.NotNull;

/**
 * DTO cho việc thay đổi trạng thái đơn hàng với validation nghiệp vụ
 */
@Data
@Getter
@Setter
public class OrderStatusTransitionRequest {
    
    @NotNull(message = "ID đơn hàng không được để trống")
    private Integer orderId;
    
    @NotNull(message = "Trạng thái hiện tại không được để trống")
    private OrderStatus currentStatus;
    
    @NotNull(message = "Trạng thái mới không được để trống")
    private OrderStatus newStatus;
    
    @NotNull(message = "ID người thực hiện không được để trống")
    private Integer performedBy;
    
    private String reason; // Lý do thay đổi trạng thái (optional)
    private String notes;  // Ghi chú thêm (optional)
    
    // Thông tin thêm cho các trường hợp đặc biệt
    private Integer staffId; // ID nhân viên (cho đơn tại quầy)
    private String trackingNumber; // Mã vận đơn (cho trạng thái SHIPPED)
}
