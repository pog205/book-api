package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.CounterSaleRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.CounterSaleResponse;
import org.datn.bookstation.dto.response.OrderResponse;

/**
 *  Service cho bán hàng tại quầy (Counter Sales)
 * 
 * Tính năng chính:
 * 1. Tạo đơn hàng tại quầy
 * 2. Tính toán giá trước khi tạo đơn 
 * 3. Hủy đơn hàng tại quầy
 * 4. Xem chi tiết đơn hàng tại quầy
 * 
 * Quy tắc nghiệp vụ:
 * - OrderType = "COUNTER"
 * - Có thể bán cho khách vãng lai (không cần userId)
 * - Không cần địa chỉ giao hàng
 * - Trạng thái mặc định: DELIVERED (thanh toán và nhận hàng ngay)
 * - Vẫn áp dụng voucher, flash sale như đơn hàng online
 * - Hủy đơn chỉ trong 24h đầu (để xử lý sai sót)
 */
public interface CounterSaleService {
    
    /**
     * Tạo đơn hàng tại quầy
     * @param request Thông tin đơn hàng tại quầy
     * @return Thông tin đơn hàng đã tạo
     */
    ApiResponse<CounterSaleResponse> createCounterSale(CounterSaleRequest request);
    
    /**
     * Tính toán giá tại quầy (preview)
     * @param request Thông tin đơn hàng cần tính toán
     * @return Thông tin giá đã tính toán
     */
    ApiResponse<CounterSaleResponse> calculateCounterSale(CounterSaleRequest request);
    
    /**
     * Xem chi tiết đơn hàng tại quầy
     * @param orderId ID đơn hàng
     * @return Chi tiết đơn hàng
     */
    ApiResponse<OrderResponse> getCounterSaleDetails(Integer orderId);
    
    /**
     * Hủy đơn hàng tại quầy (chỉ trong 24h)
     * @param orderId ID đơn hàng
     * @param staffId ID nhân viên thực hiện hủy
     * @param reason Lý do hủy
     * @return Kết quả hủy đơn
     */
    ApiResponse<OrderResponse> cancelCounterSale(Integer orderId, Integer staffId, String reason);
}
