package org.datn.bookstation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.datn.bookstation.entity.enums.AddressType;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Size(max = 100)
    @Nationalized
    @Column(name = "province_name", length = 100)
    private String provinceName;

    @Column(name = "province_id")
    private Integer provinceId;

    @Size(max = 100)
    @Nationalized
    @Column(name = "district_name", length = 100)
    private String districtName;

    @Column(name = "district_id")
    private Integer districtId;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ward_name", length = 100)
    private String wardName;

    @Column(name = "ward_code")
    private String wardCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", length = 20)
    private AddressType addressType;

    @ColumnDefault("0")
    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @ColumnDefault("1")
    @Column(name = "status")
    private Byte status;

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