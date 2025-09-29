package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Review;
import org.datn.bookstation.entity.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer>, JpaSpecificationExecutor<Review> {
    
    // Tìm review theo bookId và status
    List<Review> findByBookIdAndReviewStatusIn(Integer bookId, List<ReviewStatus> statuses);
    
    // Tìm tất cả review published của một book  
    List<Review> findByBookId(Integer bookId);

    // Kiểm tra xem user đã viết review cho sách này chưa
    boolean existsByBookIdAndUserId(Integer bookId, Integer userId);

    // Kiểm tra review thuộc về user hay không (để cho phép sửa)
    boolean existsByIdAndUserId(Integer id, Integer userId);
    
    // Tìm review theo userId
    List<Review> findByUserId(Integer userId);

    // Tìm review theo id và user
    Review findByIdAndUserId(Integer id, Integer userId);

    // Tìm review theo bookId và userId
    Review findByBookIdAndUserId(Integer bookId, Integer userId);
    
    // Tìm danh sách review theo bookId và userId (để xử lý trường hợp trùng lặp)
    List<Review> findAllByBookIdAndUserId(Integer bookId, Integer userId);

    // Đếm tổng theo status
    long countByReviewStatus(ReviewStatus status);
    long countByReviewStatusIn(List<ReviewStatus> statuses);
    
    /**
     * Lấy danh sách book ID có tỉ lệ đánh giá tích cực >= threshold
     * Chỉ tính các review đã APPROVED và EDITED (không tính PENDING, REJECTED, HIDDEN)
     */
    @Query(value = """
            SELECT book_id 
            FROM (
                SELECT 
                    book_id,
                    COUNT(*) as total_reviews,
                    COUNT(CASE WHEN is_positive = 1 THEN 1 END) as positive_reviews,
                    ROUND((COUNT(CASE WHEN is_positive = 1 THEN 1 END) * 100.0 / COUNT(*)), 2) as positive_percentage
                FROM review 
                WHERE review_status IN ('APPROVED', 'EDITED')
                    AND is_positive IS NOT NULL
                GROUP BY book_id
                HAVING COUNT(*) >= :minReviews 
                    AND (COUNT(CASE WHEN is_positive = 1 THEN 1 END) * 100.0 / COUNT(*)) >= :threshold
            ) as book_stats
            ORDER BY positive_percentage DESC
            """, nativeQuery = true)
    List<Integer> findBookIdsWithHighPositiveRating(@Param("threshold") double threshold, 
                                                   @Param("minReviews") int minReviews);

    /**
     * 📊 Lấy thông tin sentiment chi tiết cho danh sách sách (query đơn giản hóa)
     */
    @Query(value = """
            SELECT 
                book_id,
                ROUND(AVG(CAST(rating as float)), 2) as average_rating,
                COUNT(*) as total_reviews,
                COUNT(CASE WHEN is_positive = 1 THEN 1 END) as positive_reviews
            FROM review 
            WHERE review_status IN ('APPROVED', 'EDITED')
                AND is_positive IS NOT NULL
                AND book_id IN :bookIds
            GROUP BY book_id
            """, nativeQuery = true)
    List<Object[]> findSimpleSentimentStatsByBookIds(@Param("bookIds") List<Integer> bookIds);

    /**
     * 🔍 DEBUG: Query đơn giản để test dữ liệu review
     */
    @Query(value = """
            SELECT book_id, rating, is_positive, review_status 
            FROM review 
            WHERE book_id = :bookId
            """, nativeQuery = true)
    List<Object[]> findBasicReviewDataByBookId(@Param("bookId") Integer bookId);
}
