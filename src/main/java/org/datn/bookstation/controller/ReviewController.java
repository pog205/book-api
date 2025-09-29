package org.datn.bookstation.controller;

import org.datn.bookstation.dto.request.ReviewRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ReviewResponse;
import org.datn.bookstation.entity.enums.ReviewStatus;
import org.datn.bookstation.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ApiResponse<PaginationResponse<ReviewResponse>> getAllWithFilter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer bookId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false, defaultValue = "reviewDate") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {
        return reviewService.getAllWithFilter(page, size, rating, bookId, userId, from, to, status, sortBy, sortDirection);
    }


    @PostMapping
    public ApiResponse<ReviewResponse> create(@RequestBody ReviewRequest request) {
        return reviewService.createReview(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<ReviewResponse> update(@PathVariable Integer id, @RequestBody ReviewRequest request) {
        return reviewService.updateReview(request, id);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ReviewResponse> toggleStatus(@PathVariable Integer id) {
        return reviewService.toggleStatus(id);
    }

    @GetMapping("/stats")
    public ApiResponse<org.datn.bookstation.dto.response.ReviewStatsResponse> getStats() {
        return reviewService.getStats();
    }
} 