package org.datn.bookstation.repository;

import org.datn.bookstation.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Integer> {
    
    // Tìm các yêu cầu hoàn trả theo user
    List<RefundRequest> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    // Tìm các yêu cầu hoàn trả theo đơn hàng
    List<RefundRequest> findByOrderIdOrderByCreatedAtDesc(Integer orderId);
    
    // Tìm các yêu cầu hoàn trả theo trạng thái
    List<RefundRequest> findByStatusOrderByCreatedAtDesc(RefundRequest.RefundStatus status);
    
    // Admin: Lấy tất cả yêu cầu hoàn trả đang chờ phê duyệt
    @Query("SELECT r FROM RefundRequest r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<RefundRequest> findPendingRefundRequests();
    
    // Kiểm tra xem đơn hàng đã có yêu cầu hoàn trả chưa
    @Query("SELECT COUNT(r) > 0 FROM RefundRequest r WHERE r.order.id = :orderId AND r.status IN ('PENDING', 'APPROVED')")
    boolean existsActiveRefundRequestForOrder(@Param("orderId") Integer orderId);
}
