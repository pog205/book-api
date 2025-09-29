package org.datn.bookstation.specification;

import org.datn.bookstation.entity.FlashSale;
import org.springframework.data.jpa.domain.Specification;

public class FlashSaleSpecification {
    public static Specification<FlashSale> filterBy(
            String name,
            Long from,
            Long to,
            Byte status
    ) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (name != null && !name.isEmpty()) {
                predicate = cb.and(predicate, cb.like(root.get("name"), "%" + name + "%"));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startTime"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("endTime"), to));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
    }
}
