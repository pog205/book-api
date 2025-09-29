package org.datn.bookstation.specification;

import org.datn.bookstation.entity.Review;
import org.datn.bookstation.entity.enums.ReviewStatus;
import org.springframework.data.jpa.domain.Specification;
import java.util.Arrays;

public class ReviewSpecification {
    public static Specification<Review> filterBy(Integer rating,
                                                Integer bookId,
                                                Integer userId,
                                                Long from,
                                                Long to,
                                                ReviewStatus status) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (rating != null) {
                predicate = cb.and(predicate, cb.equal(root.get("rating"), rating));
            }
            if (bookId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("book").get("id"), bookId));
            }
            if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("user").get("id"), userId));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("reviewDate"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("reviewDate"), to));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("reviewStatus"), status));
            }
            return predicate;
        };
    }

    public static Specification<Review> filterByPublishedStatus(Integer rating,
                                                                Integer bookId,
                                                                Integer userId,
                                                                Long from,
                                                                Long to) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (rating != null) {
                predicate = cb.and(predicate, cb.equal(root.get("rating"), rating));
            }
            if (bookId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("book").get("id"), bookId));
            }
            if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("user").get("id"), userId));
            }
            if (from != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("reviewDate"), from));
            }
            if (to != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("reviewDate"), to));
            }
            // Chỉ lấy review có status APPROVED hoặc EDITED
            predicate = cb.and(predicate, 
                root.get("reviewStatus").in(Arrays.asList(ReviewStatus.APPROVED, ReviewStatus.EDITED)));
            return predicate;
        };
    }
} 