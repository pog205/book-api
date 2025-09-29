package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.request.ReviewRequest;
import org.datn.bookstation.dto.response.ReviewResponse;
import org.datn.bookstation.entity.Review;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(source = "book.id", target = "bookId")
    @Mapping(source = "book.bookName", target = "bookName")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "reviewStatus", target = "reviewStatus")
    ReviewResponse toResponse(Review review);

    //Xung đột do @Builder của lombok, thêm dòng này
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "reviewDate", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "reviewStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Review toReview(ReviewRequest request);

    @AfterMapping
    default void setReviewStatus(@MappingTarget Review review, ReviewRequest request) {
        System.out.println("DEBUG: ReviewRequest.reviewStatus = " + request.getReviewStatus());
        if (request.getReviewStatus() != null) {
            review.setReviewStatus(org.datn.bookstation.entity.enums.ReviewStatus.valueOf(request.getReviewStatus()));
        } else {
            review.setReviewStatus(org.datn.bookstation.entity.enums.ReviewStatus.PENDING);
        }
    }
} 