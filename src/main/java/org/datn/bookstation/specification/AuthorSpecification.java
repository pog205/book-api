package org.datn.bookstation.specification;

import jakarta.persistence.criteria.Predicate;
import org.datn.bookstation.entity.Author;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuthorSpecification {
    public static Specification<Author> filterBy(String searchText, Byte status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            //Danh sách các điều kiện lọc (predicates) được tạo ra dựa trên tham số đầu vào.
            if (searchText != null && !searchText.trim().isEmpty()) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("authorName")), searchPattern);
                Predicate biographyPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("biography")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, biographyPredicate));
            }
            if(status!=null){
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    ;
}
