package org.datn.bookstation.specification;

import org.datn.bookstation.entity.FlashSaleItem;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;

public class FlashSaleItemSpecification {
    public static Specification<FlashSaleItem> filterBy(Integer flashSaleId, String bookName, Byte status,
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minPercent, BigDecimal maxPercent,
            Integer minQuantity, Integer maxQuantity) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (flashSaleId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("flashSale").get("id"), flashSaleId));
            }

            if (bookName != null && !bookName.isEmpty()) {
                String likeText = "%" + bookName.toLowerCase() + "%";
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("book").get("bookName")), likeText));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("discountPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("discountPrice"), maxPrice));
            }
            if (minPercent != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("discountPercentage"), minPercent));
            }
            if (maxPercent != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("discountPercentage"), maxPercent));
            }
            if (minQuantity != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("stockQuantity"), minQuantity));
            }
            if (maxQuantity != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("stockQuantity"), maxQuantity));
            }
            return predicate;
        };
    }
}