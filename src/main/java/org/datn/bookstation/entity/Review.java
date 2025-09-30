package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import org.datn.bookstation.entity.enums.ReviewStatus;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    Integer rating;

    @Nationalized
    String comment;

    @Column(name = "review_date", nullable = false)
    Long reviewDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 20)
    ReviewStatus reviewStatus;

    @Column(name = "is_positive")
    Boolean isPositive; // true = tích cực, false = tiêu cực, null = không xác định

    Long createdAt;

    Long updatedAt;

    Long createdBy;

    Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = System.currentTimeMillis();
    }
}