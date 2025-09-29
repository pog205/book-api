package org.datn.bookstation.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private Integer bookId;

    @NotNull
    private Integer userId;

    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

    private Boolean isPositive; // true = tích cực, false = tiêu cực, null = không xác định

    private String reviewStatus; // giá trị của ReviewStatus (PENDING, APPROVED, ...). Có thể null khi tạo mới.
} 