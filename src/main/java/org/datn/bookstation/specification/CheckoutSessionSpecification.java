package org.datn.bookstation.specification;

import org.datn.bookstation.entity.CheckoutSession;
import org.springframework.data.jpa.domain.Specification;

public class CheckoutSessionSpecification {

    public static Specification<CheckoutSession> hasUserId(Integer userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<CheckoutSession> hasStatus(Byte status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<CheckoutSession> createdBetween(Long startDate, Long endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null || endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
        };
    }

    public static Specification<CheckoutSession> isActive() {
        return (root, query, criteriaBuilder) -> {
            long currentTime = System.currentTimeMillis();
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("status"), (byte) 1),
                criteriaBuilder.greaterThan(root.get("expiresAt"), currentTime)
            );
        };
    }

    public static Specification<CheckoutSession> isExpired() {
        return (root, query, criteriaBuilder) -> {
            long currentTime = System.currentTimeMillis();
            return criteriaBuilder.or(
                criteriaBuilder.notEqual(root.get("status"), (byte) 1),
                criteriaBuilder.lessThanOrEqualTo(root.get("expiresAt"), currentTime)
            );
        };
    }
}
