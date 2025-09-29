package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.ReviewRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ReviewResponse;

public interface BookReviewService {

    ApiResponse<PaginationResponse<ReviewResponse>> getAllReviews(Integer bookId, int page, int size, Integer rating, Long from, Long to);

    ApiResponse<PaginationResponse<ReviewResponse>> getPublishedReviews(Integer bookId, int page, int size, Integer rating, Long from, Long to, String sortBy, String sortDirection);

    ApiResponse<Double> getAverageRating(Integer bookId);

    ApiResponse<Double> getPublishedAverageRating(Integer bookId);

    ApiResponse<ReviewResponse> createReview(Integer bookId, ReviewRequest request);

    ApiResponse<ReviewResponse> updateReview(Integer bookId, Integer reviewId, ReviewRequest request);

    ApiResponse<Boolean> canCreate(Integer bookId, Integer userId);

    ApiResponse<Boolean> canEdit(Integer bookId, Integer userId);

    ApiResponse<Boolean> hasPurchased(Integer bookId, Integer userId);

    ApiResponse<ReviewResponse> getReviewByUser(Integer bookId, Integer userId);
} 