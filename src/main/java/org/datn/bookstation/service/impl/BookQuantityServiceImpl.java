package org.datn.bookstation.service.impl;

import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.OrderDetail;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.FlashSaleItemRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.service.BookQuantityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *  DEPRECATED: BookQuantityService implementation
 * 
 * Service này đã được thay thế bởi BookProcessingQuantityService
 * với cách tiếp cận real-time calculation từ database.
 * 
 * Chỉ giữ lại để tương thích với code cũ, nhưng các method
 * sẽ không thực hiện logic gì vì processing quantity được
 * tính real-time từ OrderDetail.
 */
@Service
@AllArgsConstructor
@Transactional
public class BookQuantityServiceImpl implements BookQuantityService {
    
    private final BookRepository bookRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final OrderDetailRepository orderDetailRepository;
    
    @Override
    public void increaseProcessingQuantity(List<OrderDetail> orderDetails) {
        //  KHÔNG CẦN LÀM GÌ - Sử dụng real-time calculation
        // Processing quantity được tính từ database qua BookProcessingQuantityService
    }
    
    @Override
    public void moveProcessingToSold(List<OrderDetail> orderDetails) {
        //  CHỈ trừ stock quantity khi giao hàng thành công - KHÔNG cộng sold count (đã có trong OrderServiceImpl)
        for (OrderDetail detail : orderDetails) {
            if (detail.getBook() != null) {
                var book = bookRepository.findById(detail.getBook().getId()).orElse(null);
                if (book != null) {
                    int newStock = Math.max(0, book.getStockQuantity() - detail.getQuantity());
                    book.setStockQuantity(newStock);
                    //  KHÔNG cộng sold count nữa - đã xử lý trong OrderServiceImpl.handleDeliveredBusinessLogic()
                    bookRepository.save(book);
                }
            }
            
            if (detail.getFlashSaleItem() != null) {
                var flashSaleItem = flashSaleItemRepository.findById(detail.getFlashSaleItem().getId()).orElse(null);
                if (flashSaleItem != null) {
                    int newStock = Math.max(0, flashSaleItem.getStockQuantity() - detail.getQuantity());
                    flashSaleItem.setStockQuantity(newStock);
                    //  KHÔNG cộng flash sale sold count nữa - đã xử lý trong OrderServiceImpl.handleDeliveredBusinessLogic()
                    flashSaleItemRepository.save(flashSaleItem);
                }
            }
        }
    }
    
    @Override
    public void decreaseProcessingQuantity(List<OrderDetail> orderDetails) {
        //  KHÔNG CẦN LÀM GÌ - Sử dụng real-time calculation
        // Processing quantity được tính từ database qua BookProcessingQuantityService
    }
    
    @Override
    public void handleOrderStatusChange(Integer orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        //  KHÔNG CẦN QUẢN LÝ PROCESSING QUANTITY
        // Chỉ cần xử lý stock quantity khi giao hàng thành công
        
        if (newStatus == OrderStatus.DELIVERED) {
            // Giảm stock quantity khi giao hàng thành công
            var orderDetails = orderDetailRepository.findByOrderId(orderId);
            moveProcessingToSold(orderDetails);
        }
        
        // Các trạng thái khác không cần xử lý gì vì processing quantity
        // được tính real-time từ trạng thái đơn hàng
    }
    
    @Override
    public void handleRefund(List<OrderDetail> orderDetails, boolean isPartialRefund) {
        //  KHÔNG CẦN LÀM GÌ - Refund không ảnh hưởng processing quantity
        // Vì processing quantity được tính real-time từ trạng thái đơn hàng
    }
}