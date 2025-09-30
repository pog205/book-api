package org.datn.bookstation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.enums.RewardType;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

/**
 * Entity cho Phần thưởng trong chiến dịch mở hộp
 */
@Entity
@Table(name = "reward")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RewardType type; // voucher, points, none

    @Nationalized
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Nationalized
    @Column(name = "description")
    private String description;

    // Chỉ sử dụng khi type = VOUCHER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    // Chỉ sử dụng khi type = POINTS
    @Column(name = "point_value")
    private Integer pointValue;

    @Column(name = "stock", nullable = false)
    private Integer stock; // Số lượng còn lại có thể mở

    @Column(name = "probability", nullable = false)
    private BigDecimal probability; // Tỷ lệ trúng (0-100)

    @Column(name = "status", nullable = false)
    private Byte status = 1; // 1: active, 0: inactive

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        // stock sẽ được set trực tiếp khi tạo reward
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}
