package org.datn.bookstation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "refund_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_request_id")
    private RefundRequest refundRequest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book; // Sách được hoàn trả
    
    @Column(name = "refund_quantity")
    private Integer refundQuantity; // Số lượng hoàn trả
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice; // Giá đơn vị lúc mua
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount; // Tổng tiền = refundQuantity * unitPrice
    
    @Column(name = "reason", columnDefinition = "NVARCHAR(500)")
    private String reason; // Lý do hoàn trả sản phẩm này
    
    @Column(name = "created_at")
    private Long createdAt;
}
