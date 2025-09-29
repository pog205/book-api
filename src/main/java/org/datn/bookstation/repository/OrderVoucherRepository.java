package org.datn.bookstation.repository;

import org.datn.bookstation.entity.OrderVoucher;
import org.datn.bookstation.entity.OrderVoucherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderVoucherRepository extends JpaRepository<OrderVoucher, OrderVoucherId> {
    
    @Query("SELECT ov FROM OrderVoucher ov WHERE ov.order.id = :orderId")
    List<OrderVoucher> findByOrderId(@Param("orderId") Integer orderId);
    
    @Query("SELECT ov FROM OrderVoucher ov WHERE ov.voucher.id = :voucherId")
    List<OrderVoucher> findByVoucherId(@Param("voucherId") Integer voucherId);
}
