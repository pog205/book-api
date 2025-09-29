package org.datn.bookstation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @Nationalized
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 255)
    @Nationalized
    @Column(name = "password", nullable = true)
    private String password;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Size(max = 100)
    @Nationalized
    @Column(name = "full_name", nullable = true, length = 100)
    private String fullName;

    @Size(max = 20)
    @Nationalized
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @ColumnDefault("1")
    @Column(name = "status")
    private Byte status;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "total_point")
    private Integer totalPoint;

    @ColumnDefault("0")
    @Column(name = "email_verified")
    private Byte emailVerified;

    @Column(name = "total_spent", precision = 10, scale = 2)
    private BigDecimal totalSpent;

    @ColumnDefault("0")
    @Column(name = "isRetail")
    private Byte isRetail;

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