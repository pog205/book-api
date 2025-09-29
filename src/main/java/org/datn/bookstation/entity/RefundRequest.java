package org.datn.bookstation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "refund_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Người yêu cầu hoàn trả
    
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type")
    private RefundType refundType; // PARTIAL, FULL
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RefundStatus status; // PENDING, APPROVED, REJECTED, COMPLETED
    
    @Column(name = "reason", columnDefinition = "NVARCHAR(500)")
    private String reason; // Lý do hoàn trả
    
    @Column(name = "customer_note", columnDefinition = "NVARCHAR(1000)")
    private String customerNote; // Ghi chú từ khách hàng
    
    @Column(name = "admin_note", columnDefinition = "NVARCHAR(1000)")
    private String adminNote; // Ghi chú từ admin
    
    // ✅ THÊM MỚI: Thông tin từ chối chi tiết
    @Column(name = "reject_reason", columnDefinition = "NVARCHAR(100)")
    private String rejectReason; // Lý do từ chối (enum code)
    
    @Column(name = "reject_reason_display", columnDefinition = "NVARCHAR(200)")
    private String rejectReasonDisplay; // Hiển thị lý do từ chối
    
    @Column(name = "suggested_action", columnDefinition = "NVARCHAR(500)")
    private String suggestedAction; // Gợi ý hành động cho khách hàng
    
    @Column(name = "rejected_at")
    private Long rejectedAt; // Thời gian từ chối
    
    // ✅ IMAGES/VIDEOS evidence
    @ElementCollection
    @CollectionTable(name = "refund_evidence_images", joinColumns = @JoinColumn(name = "refund_request_id"))
    @Column(name = "image_url")
    private List<String> evidenceImages; // Ảnh bằng chứng
    
    @ElementCollection
    @CollectionTable(name = "refund_evidence_videos", joinColumns = @JoinColumn(name = "refund_request_id"))
    @Column(name = "video_url") 
    private List<String> evidenceVideos; // Video bằng chứng
    
    @Column(name = "total_refund_amount")
    private BigDecimal totalRefundAmount; // Tổng số tiền hoàn
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Admin phê duyệt
    
    @Column(name = "created_at")
    private Long createdAt;
    
    @Column(name = "updated_at")
    private Long updatedAt;
    
    @Column(name = "approved_at")
    private Long approvedAt;
    
    @Column(name = "completed_at")
    private Long completedAt;
    
    // ✅ Quan hệ với RefundItem (chi tiết sản phẩm hoàn trả)
    @OneToMany(mappedBy = "refundRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RefundItem> refundItems;
    
    public enum RefundType {
        PARTIAL("Hoàn trả một phần"),
        FULL("Hoàn trả toàn bộ");
        
        private final String displayName;
        
        RefundType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum RefundStatus {
        PENDING("Chờ phê duyệt"),
        APPROVED("Đã phê duyệt"),
        REJECTED("Từ chối"),
        COMPLETED("Hoàn thành");
        
        private final String displayName;
        
        RefundStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
