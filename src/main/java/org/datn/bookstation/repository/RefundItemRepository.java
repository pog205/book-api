package org.datn.bookstation.repository;

import org.datn.bookstation.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundItemRepository extends JpaRepository<RefundItem, Integer> {
    
    // Tìm các item hoàn trả theo refund request
    List<RefundItem> findByRefundRequestId(Integer refundRequestId);
    
    // Tìm các item hoàn trả theo sách
    List<RefundItem> findByBookId(Integer bookId);
    
    //  THÊM MỚI: Tìm các item hoàn trả theo đơn hàng và sách
    @Query("SELECT ri FROM RefundItem ri " +
           "JOIN ri.refundRequest rr " +
           "WHERE rr.order.id = :orderId AND ri.book.id = :bookId " +
           "AND rr.status IN ('APPROVED', 'COMPLETED')")
    List<RefundItem> findByOrderIdAndBookId(@Param("orderId") Integer orderId, @Param("bookId") Integer bookId);
}
