package org.datn.bookstation.entity.enums;

public enum GiftDeliveryMethod {
    ONLINE_SHIPPING,    // Giao hàng online (tạo Order)
    STORE_PICKUP,       // Nhận tại cửa hàng (không cần Order)
    AUTO_DELIVERY,      // Tự động giao (voucher, điểm thưởng)
    DIGITAL_DELIVERY,   // Giao hàng số (voucher, điểm)
    DIRECT_HANDOVER     // Trao tay trực tiếp (sự kiện offline)
}
