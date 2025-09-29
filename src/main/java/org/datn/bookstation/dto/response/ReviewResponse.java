package org.datn.bookstation.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
    private Integer id;
    private Integer bookId;
    private String bookName;
    private Integer userId;
    private String userName;
    private String userEmail;
    private Integer rating;
    private String comment;
    private Boolean isPositive; // true = tích cực, false = tiêu cực, null = không xác định
    private Long reviewDate;
    private String reviewStatus;
    private Long updatedAt;
}
