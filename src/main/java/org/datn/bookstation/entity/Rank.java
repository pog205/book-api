package org.datn.bookstation.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "rank")
public class Rank {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Size(max = 100)
    @Nationalized
    @Column(name = "rank_name", length = 100)
    private String rankName;

    @Column(name = "min_spent", precision = 10, scale = 2)
    private BigDecimal minSpent;

    @Column(name = "point_multiplier", precision = 5, scale = 2)
    private BigDecimal pointMultiplier;

    @NotNull
    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    @Column(name = "status")
    private Byte status;


}