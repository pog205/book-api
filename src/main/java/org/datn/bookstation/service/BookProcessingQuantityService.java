package org.datn.bookstation.service;

import java.util.List;

/**
 * Service tính số lượng đang xử lý real-time từ database
 * Thay thế cho việc dùng cột processingQuantity
 */
public interface BookProcessingQuantityService {
    
    /**
     * Tính số lượng đang xử lý của một sách từ các đơn hàng
     */
    Integer getProcessingQuantity(Integer bookId);
    
    /**
     * Tính số lượng đang xử lý của flash sale item
     */
    Integer getFlashSaleProcessingQuantity(Integer flashSaleItemId);
    
    /**
     * Lấy số lượng đang xử lý cho nhiều sách cùng lúc
     */
    java.util.Map<Integer, Integer> getProcessingQuantities(List<Integer> bookIds);
    
    /**
     * Kiểm tra sách có đủ tồn kho để đặt không (tồn kho - đang xử lý)
     */
    boolean hasAvailableStock(Integer bookId, Integer requestedQuantity);
}
