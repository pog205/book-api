package org.datn.bookstation.entity.enums;

public enum OrderStatus {
    PENDING,                        // Chờ xử lý
    CONFIRMED,                      // Đã xác nhận
    SHIPPED,                        // Đang giao hàng
    DELIVERED,                      // Đã giao hàng thành công
    DELIVERY_FAILED,                // Giao hàng thất bại
    REDELIVERING,                   // Đang giao lại (sau khi giao thất bại)
    RETURNING_TO_WAREHOUSE,         // Đang trả về kho (khách không nhận hàng)
    CANCELED,                       // Đã hủy
    
    //  LUỒNG HOÀN TRẢ THỰC TẾ
    REFUND_REQUESTED,               // Yêu cầu hoàn trả (khách tạo yêu cầu)
    AWAITING_GOODS_RETURN,          // Đang chờ lấy hàng hoàn trả (admin đã chấp nhận)
    GOODS_RECEIVED_FROM_CUSTOMER,   // Đã nhận hàng hoàn trả từ khách
    GOODS_RETURNED_TO_WAREHOUSE,    // Hàng đã về kho (kiểm tra xong)
    REFUNDING,                      // Đang hoàn tiền (admin xử lý hoàn tiền)
    REFUNDED,                       // Đã hoàn tiền hoàn tất
    PARTIALLY_REFUNDED              // Hoàn tiền một phần
}