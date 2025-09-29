package org.datn.bookstation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.datn.bookstation.entity.enums.BoxOpenType;

/**
 * Entity lưu lịch sử mở hộp
 */
@Entity
@Table(name = "box_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoxHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(name = "open_type", nullable = false, length = 20)
    private BoxOpenType openType; // FREE, POINT

    @Column(name = "open_date", nullable = false)
    private Long openDate;

    // Phần thưởng trúng (nullable nếu không trúng gì)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    // Giá trị phần thưởng (để lưu lại cho thống kê)
    @Column(name = "reward_value")
    private Integer rewardValue; // Điểm hoặc voucher value

    @Column(name = "points_spent")
    private Integer pointsSpent; // Số điểm đã tiêu để mở (nếu type = POINT)

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
        this.openDate = System.currentTimeMillis();
    }
}
