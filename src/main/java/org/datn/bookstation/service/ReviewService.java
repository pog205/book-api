package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.ReviewRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ReviewResponse;
import org.datn.bookstation.dto.response.ReviewStatsResponse;
import org.datn.bookstation.entity.enums.ReviewStatus;

public interface ReviewService {
    ApiResponse<PaginationResponse<ReviewResponse>> getAllWithFilter(int page,
                                                                     int size,
                                                                     Integer rating,
                                                                     Integer bookId,
                                                                     Integer userId,
                                                                     Long from,
                                                                     Long to,
                                                                     ReviewStatus status,
                                                                     String sortBy,
                                                                     String sortDirection);
    ApiResponse<ReviewResponse> createReview(ReviewRequest request);
    ApiResponse<ReviewResponse> updateReview(ReviewRequest request, Integer id);
    ApiResponse<ReviewResponse> toggleStatus(Integer id);
    ApiResponse<ReviewStatsResponse> getStats();

}
