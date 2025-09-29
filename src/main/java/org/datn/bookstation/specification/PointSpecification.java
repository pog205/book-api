package org.datn.bookstation.specification;

import org.datn.bookstation.entity.Point;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class PointSpecification {
    
    public static Specification<Point> filterBy(String orderCode, String email, Byte status, Integer pointSpent) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (orderCode != null && !orderCode.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("order").get("code")), 
                    "%" + orderCode.toLowerCase() + "%"
                ));
            }
            
            if (email != null && !email.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("user").get("email")), 
                    "%" + email.toLowerCase() + "%"
                ));
            }
            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            if (pointSpent != null) {
                predicates.add(criteriaBuilder.equal(root.get("pointSpent"), pointSpent));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
