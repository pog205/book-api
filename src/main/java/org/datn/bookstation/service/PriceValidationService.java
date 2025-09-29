package org.datn.bookstation.service;
import org.datn.bookstation.dto.request.PriceValidationRequest;

import org.datn.bookstation.dto.response.ApiResponse;

import java.util.List;

/**
 * Service validation giá sản phẩm khi đặt hàng
 */
public interface PriceValidationService {
    
    /**
     * Validate giá sản phẩm từ frontend với giá backend hiện tại
     * @param orderDetails Danh sách chi tiết đơn hàng từ frontend
     * @return ApiResponse chứa thông tin validation
     */
    ApiResponse<String> validateProductPrices(List<PriceValidationRequest> priceValidationRequests);
    
    /**
     *  ENHANCED: Validate giá và số lượng flash sale
     * @param priceValidationRequests Danh sách validation request với quantity
     * @param userId ID user để check giới hạn flash sale
     * @return ApiResponse chứa thông tin validation
     */
    ApiResponse<String> validateProductPricesAndQuantities(List<PriceValidationRequest> priceValidationRequests, Integer userId);
    
    /**
     * Validate một sản phẩm cụ thể
     * @param bookId ID của sách
     * @param frontendPrice Giá từ frontend
     * @return Thông báo lỗi (null nếu hợp lệ)
     */
    String validateSingleProductPrice(Integer bookId, java.math.BigDecimal frontendPrice);
    
    /**
     *  ENHANCED: Validate một sản phẩm với số lượng và flash sale limit
     * @param bookId ID của sách
     * @param frontendPrice Giá từ frontend
     * @param quantity Số lượng muốn mua
     * @param userId ID user để check giới hạn flash sale
     * @return Thông báo lỗi (null nếu hợp lệ)
     */
    String validateSingleProductPriceAndQuantity(Integer bookId, java.math.BigDecimal frontendPrice, Integer quantity, Integer userId);
}
