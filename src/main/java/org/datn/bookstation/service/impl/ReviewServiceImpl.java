package org.datn.bookstation.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.datn.bookstation.dto.request.ReviewRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ReviewResponse;
import org.datn.bookstation.dto.response.ReviewStatsResponse;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.Review;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.enums.ReviewStatus;
import org.datn.bookstation.mapper.ReviewMapper;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.ReviewRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.ReviewService;
import org.datn.bookstation.specification.ReviewSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public ApiResponse<PaginationResponse<ReviewResponse>> getAllWithFilter(int page, int size, Integer rating, Integer bookId, Integer userId, Long from, Long to, ReviewStatus status, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.valueOf(sortDirection), sortBy));
        Specification<Review> specification = ReviewSpecification.filterBy(rating, bookId, userId, from, to, status);
        Page<Review> reviewPage = reviewRepository.findAll(specification, pageable);

        List<ReviewResponse> content = reviewPage.getContent().stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());

        PaginationResponse<ReviewResponse> pagination = PaginationResponse.<ReviewResponse>builder()
                .content(content)
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .build();

        return new ApiResponse<>(200, "Lấy danh sách đánh giá thành công", pagination);
    }

    

    @Override
    public ApiResponse<ReviewResponse> createReview(ReviewRequest request) {
        Book book = bookRepository.findById(request.getBookId()).orElse(null);
        if (book == null) {
            return new ApiResponse<>(404, "Sách không tồn tại", null);
        }
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) {
            return new ApiResponse<>(404, "Người dùng không tồn tại", null);
        }

        // Check user đã đánh giá sách này chưa
        boolean alreadyReviewed = reviewRepository.existsByBookIdAndUserId(request.getBookId(), request.getUserId());
        if (alreadyReviewed) {
            return new ApiResponse<>(400, "Người dùng đã có đánh giá cho sách này", null);
        }

        Review review = reviewMapper.toReview(request);
        review.setBook(book);
        review.setUser(user);
        reviewRepository.save(review);
        return new ApiResponse<>(201, "Tạo đánh giá thành công", reviewMapper.toResponse(review));
    }

    @Override
    public ApiResponse<ReviewResponse> updateReview(ReviewRequest request, Integer id) {
        Review existing = reviewRepository.findById(id).orElse(null);
        if (existing == null) {
            return new ApiResponse<>(404, "Đánh giá không tồn tại", null);
        }

        if (request.getRating() != null) {
            existing.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            existing.setComment(request.getComment());
        }
        if (request.getIsPositive() != null) {
            existing.setIsPositive(request.getIsPositive());
        }
        if (request.getReviewStatus() != null) {
            existing.setReviewStatus(ReviewStatus.valueOf(request.getReviewStatus()));
        }
        reviewRepository.save(existing);
        return new ApiResponse<>(200, "Cập nhật đánh giá thành công", reviewMapper.toResponse(existing));
    }

    @Override
    public ApiResponse<ReviewResponse> toggleStatus(Integer id) {
        Review review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            return new ApiResponse<>(404, "Đánh giá không tồn tại", null);
        }
        ReviewStatus current = review.getReviewStatus();
        ReviewStatus next = current == ReviewStatus.APPROVED ? ReviewStatus.HIDDEN : ReviewStatus.APPROVED;
        review.setReviewStatus(next);
        reviewRepository.save(review);
        return new ApiResponse<>(200, "Cập nhật trạng thái đánh giá thành công", reviewMapper.toResponse(review));
    }

    @Override
    public ApiResponse<ReviewStatsResponse> getStats() {
        long total = reviewRepository.count();
        long edited = reviewRepository.countByReviewStatus(ReviewStatus.EDITED);
        long hidden = reviewRepository.countByReviewStatus(ReviewStatus.HIDDEN);
        long approved = reviewRepository.countByReviewStatus(ReviewStatus.APPROVED);

        ReviewStatsResponse stats = ReviewStatsResponse.builder()
                .total(total)
                .approved(approved)
                .edited(edited)
                .hidden(hidden)
                .build();
        return new ApiResponse<>(200, "Lấy thống kê đánh giá thành công", stats);
    }
    
}
