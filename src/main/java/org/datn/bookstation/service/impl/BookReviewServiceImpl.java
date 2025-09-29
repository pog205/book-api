package org.datn.bookstation.service.impl;

import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.request.ReviewRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ReviewResponse;
import org.datn.bookstation.entity.Review;
import org.datn.bookstation.entity.enums.ReviewStatus;
import org.datn.bookstation.mapper.ReviewMapper;
import org.datn.bookstation.repository.BookRepository;
import org.datn.bookstation.repository.OrderDetailRepository;
import org.datn.bookstation.repository.ReviewRepository;
import org.datn.bookstation.repository.UserRepository;
import org.datn.bookstation.service.BookReviewService;
import org.datn.bookstation.specification.ReviewSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.datn.bookstation.entity.Book;
import org.datn.bookstation.entity.User;

@Service
@RequiredArgsConstructor
public class BookReviewServiceImpl implements BookReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public ApiResponse<PaginationResponse<ReviewResponse>> getAllReviews(Integer bookId, int page, int size, Integer rating, Long from, Long to) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Review> spec = ReviewSpecification.filterBy(rating, bookId, null, from, to, null);
        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);
        List<ReviewResponse> content = reviewPage.getContent().stream().map(reviewMapper::toResponse).collect(Collectors.toList());
        PaginationResponse<ReviewResponse> pagination = PaginationResponse.<ReviewResponse>builder()
                .content(content)
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .build();
        return new ApiResponse<>(200, "Lấy danh sách review thành công", pagination);
    }

    @Override
    public ApiResponse<PaginationResponse<ReviewResponse>> getPublishedReviews(Integer bookId, int page, int size, Integer rating, Long from, Long to, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.valueOf(sortDirection), sortBy));
        Specification<Review> spec = ReviewSpecification.filterByPublishedStatus(rating, bookId, null, from, to);
        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);
        List<ReviewResponse> content = reviewPage.getContent().stream().map(reviewMapper::toResponse).collect(Collectors.toList());
        PaginationResponse<ReviewResponse> pagination = PaginationResponse.<ReviewResponse>builder()
                .content(content)
                .pageNumber(reviewPage.getNumber())
                .pageSize(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .build();
        return new ApiResponse<>(200, "Lấy danh sách review đã xuất bản thành công", pagination);
    }

    @Override
    public ApiResponse<Double> getAverageRating(Integer bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        double avg = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        return new ApiResponse<>(200, "Trung bình đánh giá", avg);
    }

    @Override
    public ApiResponse<Double> getPublishedAverageRating(Integer bookId) {
        List<ReviewStatus> statuses = Arrays.asList(ReviewStatus.APPROVED, ReviewStatus.EDITED);
        List<Review> reviews = reviewRepository.findByBookIdAndReviewStatusIn(bookId, statuses);
        double avg = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        return new ApiResponse<>(200, "Trung bình đánh giá (published)", avg);
    }

    @Override
    public ApiResponse<ReviewResponse> createReview(Integer bookId, ReviewRequest request) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return new ApiResponse<>(404, "Sách không tồn tại", null);
        }
        User user = request.getUserId() != null ? userRepository.findById(request.getUserId()).orElse(null) : null;
        if (user == null) {
            return new ApiResponse<>(404, "Người dùng không tồn tại", null);
        }

        // Kiểm tra quyền tạo review (đã mua và chưa review)
        boolean purchased = orderDetailRepository.existsDeliveredByUserAndBook(user.getId(), bookId);
        if (!purchased) {
            return new ApiResponse<>(400, "Chỉ người dùng đã mua sách mới có thể đánh giá", null);
        }
        boolean alreadyReview = reviewRepository.existsByBookIdAndUserId(bookId, user.getId());
        if (alreadyReview) {
            return new ApiResponse<>(400, "Người dùng đã đánh giá sách này", null);
        }

        Review review = reviewMapper.toReview(request);
        review.setBook(book);
        review.setUser(user);
        review.setReviewStatus(ReviewStatus.APPROVED);

        review = reviewRepository.save(review);
        return new ApiResponse<>(200, "Tạo review thành công", reviewMapper.toResponse(review));
    }

    @Override
    public ApiResponse<ReviewResponse> updateReview(Integer bookId, Integer reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null || !review.getBook().getId().equals(bookId)) {
            return new ApiResponse<>(404, "Review không tồn tại", null);
        }
        if (ReviewStatus.EDITED.equals(review.getReviewStatus())) {
            return new ApiResponse<>(400, "Review đã được chỉnh sửa trước đó, không thể sửa thêm", null);
        }
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        if (request.getIsPositive() != null) {
            review.setIsPositive(request.getIsPositive());
        }
        review.setReviewStatus(ReviewStatus.EDITED);
        review = reviewRepository.save(review);
        return new ApiResponse<>(200, "Cập nhật review thành công", reviewMapper.toResponse(review));
    }

    @Override
    public ApiResponse<Boolean> canCreate(Integer bookId, Integer userId) {
        boolean alreadyReview = reviewRepository.existsByBookIdAndUserId(bookId, userId);
        boolean purchased = orderDetailRepository.existsDeliveredByUserAndBook(userId, bookId);
        boolean can = !alreadyReview && purchased;
        return new ApiResponse<>(200, "Kiểm tra thành công", can);
    }

    @Override
    public ApiResponse<Boolean> canEdit(Integer bookId, Integer userId) {
        try {
            Review review = reviewRepository.findByBookIdAndUserId(bookId, userId);
            boolean canEdit = review != null && !ReviewStatus.EDITED.equals(review.getReviewStatus());
            return new ApiResponse<>(200, "Kiểm tra thành công", canEdit);
        } catch (Exception e) {
            // Nếu có nhiều review trùng lặp, lấy review đầu tiên
            List<Review> reviews = reviewRepository.findAllByBookIdAndUserId(bookId, userId);
            if (reviews.isEmpty()) {
                return new ApiResponse<>(200, "Kiểm tra thành công", false);
            }
            Review review = reviews.get(0); // Lấy review đầu tiên
            boolean canEdit = !ReviewStatus.EDITED.equals(review.getReviewStatus());
            return new ApiResponse<>(200, "Kiểm tra thành công", canEdit);
        }
    }

    @Override
    public ApiResponse<Boolean> hasPurchased(Integer bookId, Integer userId) {
        boolean purchased = orderDetailRepository.existsDeliveredByUserAndBook(userId, bookId);
        return new ApiResponse<>(200, "Kiểm tra thành công", purchased);
    }

    @Override
    public ApiResponse<ReviewResponse> getReviewByUser(Integer bookId, Integer userId) {
        try {
            Review review = reviewRepository.findByBookIdAndUserId(bookId, userId);
            if (review == null) {
                return new ApiResponse<>(200, "Người dùng chưa có đánh giá nào", null);
            }
            ReviewResponse response = reviewMapper.toResponse(review);
            return new ApiResponse<>(200, "Lấy review thành công", response);
        } catch (Exception e) {
            // Nếu có nhiều review trùng lặp, lấy review đầu tiên
            List<Review> reviews = reviewRepository.findAllByBookIdAndUserId(bookId, userId);
            if (reviews.isEmpty()) {
                return new ApiResponse<>(200, "Người dùng chưa có đánh giá nào", null);
            }
            Review review = reviews.get(0); // Lấy review đầu tiên
            ReviewResponse response = reviewMapper.toResponse(review);
            return new ApiResponse<>(200, "Lấy review thành công", response);
        }
    }
} 