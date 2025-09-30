package org.datn.bookstation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.Nationalized;

import java.util.List;

/**
 * Entity cho Chiến dịch mở hộp
 */
@Entity
@Table(name = "campaign")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "start_date", nullable = false)
    private Long startDate;

    @Column(name = "end_date", nullable = false)
    private Long endDate;

    @Column(name = "status", nullable = false)
    private Byte status = 1; // 1: active, 0: inactive

    @Column(name = "config_free_limit", nullable = false)
    private Integer configFreeLimit; // Số lượt free tối đa mỗi user

    @Column(name = "config_point_cost", nullable = false)
    private Integer configPointCost; // Số điểm cần để mở 1 hộp point

    @Nationalized
    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    // Quan hệ với Reward
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reward> rewards;

    // Quan hệ với UserCampaign
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCampaign> userCampaigns;

    // Quan hệ với BoxHistory
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BoxHistory> boxHistories;

    @PrePersist
    public void prePersist() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = System.currentTimeMillis();
    }
}
