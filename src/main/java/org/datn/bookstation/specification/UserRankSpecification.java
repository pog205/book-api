package org.datn.bookstation.specification;

import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.UserRank;
import org.datn.bookstation.entity.enums.RoleName;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class UserRankSpecification {
    public static Specification<UserRank> filterBy(Integer userId, Integer rankId, Byte status, String userEmail,
            String rankName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.join("user").get("id"), userId));
            }
            if (rankId != null) {
                predicates.add(criteriaBuilder.equal(root.join("rank").get("id"), rankId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (userEmail != null && !userEmail.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("user").get("email")),
                        "%" + userEmail.toLowerCase() + "%"));
            }
            if (rankName != null && !rankName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("rank").get("name")),
                        "%" + rankName.toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> filterBy(String text) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy user có role là CUSTOMER
            predicates.add(criteriaBuilder.equal(root.get("role").get("roleName"),
                   RoleName.CUSTOMER));

            if (text != null && !text.trim().isEmpty()) {
                Predicate fullNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")),
                        "%" + text.toLowerCase() + "%");
                Predicate phonePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("phoneNumber")),
                        "%" + text.toLowerCase() + "%");
                predicates.add(criteriaBuilder.or(fullNamePredicate, phonePredicate));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
