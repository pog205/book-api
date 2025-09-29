package org.datn.bookstation.service;

import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.AvailableStatusTransitionResponse;

/**
 * Service xử lý logic trả về danh sách trạng thái có thể chuyển
 */
public interface AvailableStatusTransitionService {
    
    /**
     * Lấy danh sách trạng thái có thể chuyển dựa vào ID đơn hàng
     * 
     * @param orderId ID đơn hàng
     * @return ApiResponse chứa danh sách trạng thái có thể chuyển
     */
    ApiResponse<AvailableStatusTransitionResponse> getAvailableTransitions(Integer orderId);
}
