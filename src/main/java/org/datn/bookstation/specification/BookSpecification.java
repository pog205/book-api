package org.datn.bookstation.specification;

import org.datn.bookstation.entity.Book;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBy(String bookName, Integer categoryId, Integer supplierId,
                                               Integer publisherId,
                                               BigDecimal minPrice, BigDecimal maxPrice, Byte status,
                                               String bookCode) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            if (bookName != null && !bookName.isEmpty()) {
                //  IMPROVED: Tách từ khóa và tìm kiếm với OR logic để tìm sách chứa BẤT KỲ từ
                // khóa nào
                String[] keywords = bookName.trim().split("\\s+");
                var bookNamePredicate = criteriaBuilder.disjunction(); // OR thay vì AND

                for (String keyword : keywords) {
                    if (!keyword.isEmpty()) {
                        bookNamePredicate = criteriaBuilder.or(bookNamePredicate,
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("bookName")),
                                        "%" + keyword.toLowerCase() + "%"));
                    }
                }

                predicates = criteriaBuilder.and(predicates, bookNamePredicate);
            }

            if (categoryId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (supplierId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("supplier").get("id"), supplierId));
            }

            if (publisherId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("publisher").get("id"), publisherId));
            }

            if (minPrice != null && maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.between(root.get("price"), minPrice, maxPrice));
            } else if (minPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            } else if (maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (status != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("status"), status));
            }

            if (bookCode != null && !bookCode.isEmpty()) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bookCode")),
                                "%" + bookCode.toLowerCase() + "%"));
            }

            return predicates;
        };
    }

    public static Specification<Book> filterBy(String bookName, Integer categoryId, Integer parentCategoryId,
                                               List<Integer> authorId,
                                               Integer publisherId,
                                               BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();
            //  Chỉ lấy sách có status = 1 (hoạt động)
            predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("status"), 1));
            if (bookName != null && !bookName.isEmpty()) {
                //  IMPROVED: Tách từ khóa và tìm kiếm với OR logic để tìm sách chứa BẤT KỲ từ
                // khóa nào
                String[] keywords = bookName.trim().split("\\s+");
                var bookNamePredicate = criteriaBuilder.disjunction(); // OR thay vì AND

                for (String keyword : keywords) {
                    if (!keyword.isEmpty()) {
                        bookNamePredicate = criteriaBuilder.or(bookNamePredicate,
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("bookName")),
                                        "%" + keyword.toLowerCase() + "%"));
                    }
                }

                predicates = criteriaBuilder.and(predicates, bookNamePredicate);
            }

            if (parentCategoryId != null) {
                System.out.println(parentCategoryId);
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("category").get("id"), parentCategoryId));
            }
            if (categoryId != null) {
                System.out.println(categoryId);
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("category").get("parentCategory").get("id"), categoryId));
            }
            if (publisherId != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("publisher").get("id"), publisherId));
            }

            if (minPrice != null && maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.between(root.get("price"), minPrice, maxPrice));
            } else if (minPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            } else if (maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            System.out.println("AUTHORiD" + authorId);

            if (authorId != null && !authorId.isEmpty()) {
                System.out.println("AUTHORiD" + authorId);
                // Join sang authorBooks, rồi lấy author.id
                var authorBookJoin = root.join("authorBooks");
                predicates = criteriaBuilder.and(predicates,
                        authorBookJoin.get("author").get("id").in(authorId));
            }

            return predicates;
        };
    }

    public static Specification<Book> filterBy(Integer categoryId, String text) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();
            //  Chỉ lấy sách có status = 1 (hoạt động)
            predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("status"), 1));
            System.out.println(categoryId);
            if (categoryId != null && categoryId != 0) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (text != null && !text.isEmpty()) {
                //  IMPROVED: Tách từ khóa và tìm kiếm với OR logic để tìm sách chứa BẤT KỲ từ
                // khóa nào
                String[] keywords = text.trim().split("\\s+");
                var textPredicate = criteriaBuilder.disjunction(); // OR thay vì AND

                for (String keyword : keywords) {
                    if (!keyword.isEmpty()) {
                        textPredicate = criteriaBuilder.or(textPredicate,
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("bookName")),
                                        "%" + keyword.toLowerCase() + "%"));
                    }
                }

                predicates = criteriaBuilder.and(predicates, textPredicate);
            }

            return predicates;
        };
    }

    public static Specification<Book> filterBy(String text) {
        return (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();
            //  Chỉ lấy sách có status = 1 (hoạt động)
            predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("status"), 1));
            if (text != null && !text.isEmpty()) {
                String likeText = "%" + text.toLowerCase() + "%";
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bookName")), likeText));
            }

            return predicates;
        };
    }
}
