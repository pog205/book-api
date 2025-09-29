package org.datn.bookstation.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ UTILITY CLASS - Hiển thị tên reason hoàn trả bằng tiếng Việt
 */
public class RefundReasonUtil {
    
    private static final Map<String, String> REASON_DISPLAY_MAP;
    
    static {
        Map<String, String> reasonMap = new HashMap<>();
        
        // ✅ CÁC LÝ DO HOÀN TRẢ PHỔ BIẾN
        reasonMap.put("WRONG_ITEM", "Sai sản phẩm");
        reasonMap.put("DAMAGED_ITEM", "Sản phẩm bị hỏng");
        reasonMap.put("DEFECTIVE_ITEM", "Sản phẩm lỗi");
        reasonMap.put("PRODUCT_DEFECT", "Sản phẩm có lỗi");
        reasonMap.put("NOT_AS_DESCRIBED", "Không đúng mô tả");
        reasonMap.put("DAMAGED_SHIPPING", "Hỏng trong quá trình vận chuyển"); //  THÊM MỚI
        reasonMap.put("POOR_QUALITY", "Chất lượng kém");
        reasonMap.put("SIZE_ISSUE", "Vấn đề về kích thước");
        reasonMap.put("COLOR_ISSUE", "Vấn đề về màu sắc");
        reasonMap.put("LATE_DELIVERY", "Giao hàng muộn");
        reasonMap.put("PACKAGING_ISSUE", "Vấn đề đóng gói");
        reasonMap.put("DUPLICATE_ORDER", "Đặt hàng trùng lặp");
        reasonMap.put("CHANGED_MIND", "Thay đổi ý kiến");
        reasonMap.put("FOUND_BETTER_PRICE", "Tìm thấy giá tốt hơn");
        reasonMap.put("NO_LONGER_NEEDED", "Không còn cần thiết");
        reasonMap.put("INCORRECT_ADDRESS", "Địa chỉ sai");
        reasonMap.put("PAYMENT_ISSUE", "Vấn đề thanh toán");
        reasonMap.put("SELLER_REQUEST", "Yêu cầu từ người bán");
        reasonMap.put("POLICY_VIOLATION", "Vi phạm chính sách");
        reasonMap.put("OUT_OF_STOCK", "Hết hàng");
        reasonMap.put("TECHNICAL_ERROR", "Lỗi kỹ thuật");
        reasonMap.put("OTHER", "Lý do khác");
        
        REASON_DISPLAY_MAP = Map.copyOf(reasonMap);
    }
    
    /**
     * Lấy tên hiển thị reason bằng tiếng Việt
     * 
     * @param reason Reason code (WRONG_ITEM, DAMAGED_ITEM, etc.)
     * @return Tên hiển thị tiếng Việt
     */
    public static String getReasonDisplayName(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return "Không xác định";
        }
        return REASON_DISPLAY_MAP.getOrDefault(reason.toUpperCase(), reason);
    }
    
    /**
     * Lấy tất cả mapping reasons
     * 
     * @return Map của tất cả reasons và display names
     */
    public static Map<String, String> getAllReasonMappings() {
        return REASON_DISPLAY_MAP;
    }
}
