package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.OrderCalculationRequest;
import org.datn.bookstation.dto.response.OrderCalculationResponse;
import org.datn.bookstation.dto.response.ApiResponse;

public interface OrderCalculationService {
    
    /**
     * Tính toán tổng tiền đơn hàng trước khi tạo đơn thực tế
     * Bao gồm tự động phát hiện flash sale và áp dụng voucher
     * 
     * @param request Thông tin đơn hàng cần tính toán
     * @return Kết quả tính toán chi tiết
     */
    ApiResponse<OrderCalculationResponse> calculateOrderTotal(OrderCalculationRequest request);
    
    /**
     * Validate các điều kiện nghiệp vụ trước khi tạo đơn
     * - Kiểm tra user tồn tại
     * - Kiểm tra sản phẩm còn tồn kho
     * - Kiểm tra flash sale còn hiệu lực
     * - Kiểm tra voucher có thể sử dụng
     * 
     * @param request Thông tin đơn hàng
     * @return True nếu hợp lệ, false nếu có lỗi
     */
    ApiResponse<String> validateOrderConditions(OrderCalculationRequest request);
}
