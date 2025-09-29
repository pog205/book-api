package org.datn.bookstation.entity.enums;

public enum GiftClaimStatus {
    PENDING,        // Chờ xử lý
    APPROVED,       // Đã duyệt, chuẩn bị giao
    ORDER_CREATED,  // Đã tạo đơn hàng giao quà
    DELIVERED,      // Đã giao thành công
    COMPLETED,      // Đã hoàn thành việc nhận quà (bao gồm cả auto delivery)
    REJECTED,       // Từ chối (hết quà, không đủ điều kiện...)
    EXPIRED         // Hết hạn claim
}
