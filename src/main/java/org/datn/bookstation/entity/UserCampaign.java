package org.datn.bookstation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entity theo dõi User trong chiến dịch mở hộp
 */
@Entity
@Table(name = "user_campaign")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCampaign {
    
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

    @Column(name = "free_opened_count", nullable = false)
    private Integer freeOpenedCount = 0; // Đã mở free bao nhiêu lần

    @Column(name = "total_opened_count", nullable = false)
    private Integer totalOpenedCount = 0; // Tổng số lần đã mở (free + point)

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }

    // Unique constraint để 1 user chỉ có 1 record per campaign
    @Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "campaign_id"})})
    public static class UserCampaignConstraint {}
}
