package org.datn.bookstation.specification;

import org.datn.bookstation.entity.Order;
import org.datn.bookstation.entity.enums.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {
    
    public static Specification<Order> filterBy(String code, Integer userId, OrderStatus orderStatus, 
                                               String orderType, Long startDate, Long endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (code != null && !code.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("code")), 
                    "%" + code.toLowerCase() + "%"
                ));
            }
            
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            }
            
            if (orderStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderStatus"), orderStatus));
            }
            
            if (orderType != null && !orderType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("orderType")), 
                    "%" + orderType.toLowerCase() + "%"
                ));
            }
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("orderStatus"), status);
    }
    
    public static Specification<Order> hasUserId(Integer userId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }
    
    public static Specification<Order> hasStaffId(Integer staffId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("staff").get("id"), staffId);
    }
    
    public static Specification<Order> createdBetween(Long startDate, Long endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
