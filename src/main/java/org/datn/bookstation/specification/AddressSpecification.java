package org.datn.bookstation.specification;

import org.datn.bookstation.entity.Address;
import org.springframework.data.jpa.domain.Specification;

public class AddressSpecification {
    public static Specification<Address> filterBy(Integer userId, Byte status) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
    }
} 