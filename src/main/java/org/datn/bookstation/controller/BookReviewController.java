package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import org.datn.bookstation.dto.request.ReviewRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.ReviewResponse;
import org.datn.bookstation.service.BookReviewService;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/{bookId}/reviews")
@RequiredArgsConstructor
public class BookReviewController {

    private final BookReviewService bookReviewService;

    // 1. Lấy toàn bộ review của sách
    @GetMapping
    public ApiResponse<PaginationResponse<ReviewResponse>> getAllReviews(
            @PathVariable Integer bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return bookReviewService.getAllReviews(bookId, page, size, rating, from, to);
    }

    // 2. Lấy review đã chấp nhận và đã chỉnh sửa
    @GetMapping("/published")
    public ApiResponse<PaginationResponse<ReviewResponse>> getPublishedReviews(
            @PathVariable Integer bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to,
            @RequestParam(required = false, defaultValue = "reviewDate") String sortBy,
            @RequestParam(required = false, defaultValue = "DESC") String sortDirection) {
        return bookReviewService.getPublishedReviews(bookId, page, size, rating, from, to, sortBy, sortDirection);
    }

    // 3. Trung bình đánh giá (toàn bộ)
    @GetMapping("/average")
    public ApiResponse<Double> getAverageRating(@PathVariable Integer bookId) {
        return bookReviewService.getAverageRating(bookId);
    }

    // 4. Trung bình đánh giá đã published
    @GetMapping("/published-average")
    public ApiResponse<Double> getPublishedAverageRating(@PathVariable Integer bookId) {
        return bookReviewService.getPublishedAverageRating(bookId);
    }

    // 5. Tạo review mới
    @PostMapping
    public ApiResponse<ReviewResponse> createReview(@PathVariable Integer bookId, @RequestBody ReviewRequest request) {
        return bookReviewService.createReview(bookId, request);
    }

    // 6. Sửa review
    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(@PathVariable Integer bookId,
                                                    @PathVariable Integer reviewId,
                                                    @RequestBody ReviewRequest request) {
        return bookReviewService.updateReview(bookId, reviewId, request);
    }

    // 7. Có thể tạo review không?
    @GetMapping("/can-create")
    public ApiResponse<Boolean> canCreate(@PathVariable Integer bookId, @RequestParam Integer userId) {
        return bookReviewService.canCreate(bookId, userId);
    }

    // 9. Kiểm tra đã mua chưa
    @GetMapping("/purchased")
    public ApiResponse<Boolean> hasPurchased(@PathVariable Integer bookId, @RequestParam Integer userId) {
        return bookReviewService.hasPurchased(bookId, userId);
    }

    // 8. Có thể sửa không?
    @GetMapping("/can-edit")
    public ApiResponse<Boolean> canEdit(@PathVariable Integer bookId, @RequestParam Integer userId) {
        return bookReviewService.canEdit(bookId, userId);
    }

    // 10. Lấy review của user (để hiển thị form sửa)
    @GetMapping("/by-user")
    public ApiResponse<ReviewResponse> getReviewByUser(@PathVariable Integer bookId, @RequestParam Integer userId) {
        return bookReviewService.getReviewByUser(bookId, userId);
    }
}
