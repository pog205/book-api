package org.datn.bookstation.specification;

import java.util.ArrayList;
import java.util.List;

import org.datn.bookstation.entity.Author;
import org.datn.bookstation.entity.Category;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public class CategorySpecification {

    public static Specification<Category> filterBy(String searchText, Byte status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            //Danh sách các điều kiện lọc (predicates) được tạo ra dựa trên tham số đầu vào.
            if (searchText != null && !searchText.trim().isEmpty()) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("categoryName")), searchPattern);
                Predicate description = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, description));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    ;
    public static Specification<Category> filterBy(String searchText) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            //Danh sách các điều kiện lọc (predicates) được tạo ra dựa trên tham số đầu vào.
            if (searchText != null && !searchText.trim().isEmpty()) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("categoryName")), searchPattern);
                Predicate description = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, description));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    ;
}
