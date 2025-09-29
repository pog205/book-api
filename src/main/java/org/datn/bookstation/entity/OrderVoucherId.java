package org.datn.bookstation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class OrderVoucherId implements Serializable {
    private static final long serialVersionUID = -2421436680464088700L;
    @NotNull
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @NotNull
    @Column(name = "voucher_id", nullable = false)
    private Integer voucherId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OrderVoucherId entity = (OrderVoucherId) o;
        return Objects.equals(this.orderId, entity.orderId) &&
                Objects.equals(this.voucherId, entity.voucherId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, voucherId);
    }

}