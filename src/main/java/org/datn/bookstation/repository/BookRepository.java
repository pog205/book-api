package org.datn.bookstation.repository;

import org.datn.bookstation.dto.request.BookFlashSalesRequest;
import org.datn.bookstation.dto.response.BookStockResponse;
import org.datn.bookstation.dto.response.TopBookSoldResponse;
import org.datn.bookstation.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    boolean existsByBookName(String bookName);

    boolean existsByBookCode(String bookCode);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b WHERE UPPER(TRIM(b.bookName)) = UPPER(TRIM(:bookName))")
    boolean existsByBookNameIgnoreCase(@Param("bookName") String bookName);

    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId")
    List<Book> findByCategoryId(@Param("categoryId") Integer categoryId);

    @Query("SELECT b FROM Book b WHERE b.supplier.id = :supplierId")
    List<Book> findBySupplierId(@Param("supplierId") Integer supplierId);

    @Query("SELECT b FROM Book b WHERE b.publisher.id = :publisherId")
    List<Book> findByPublisherId(@Param("publisherId") Integer publisherId);

    @Query("SELECT b FROM Book b WHERE b.status = 1 ORDER BY b.createdAt DESC")
    List<Book> findActiveBooks();

    /**
     * Lấy dữ liệu cho trending books với thông tin thống kê
     * Bao gồm: thông tin cơ bản của sách, số lượng đã bán, số đơn hàng, rating
     * trung bình, số review
     */
    @Query("""
            SELECT b.id as bookId,
                   b.bookName as bookName,
                   b.description as description,
                   b.price as price,
                   b.stockQuantity as stockQuantity,
                   b.bookCode as bookCode,
                   b.publicationDate as publicationDate,
                   b.createdAt as createdAt,
                   b.updatedAt as updatedAt,
                   b.category.id as categoryId,
                   b.category.categoryName as categoryName,
                   b.supplier.id as supplierId,
                   b.supplier.supplierName as supplierName,
                   COALESCE(salesData.soldCount, 0) as soldCount,
                   COALESCE(salesData.orderCount, 0) as orderCount,
                   COALESCE(reviewData.avgRating, 0.0) as avgRating,
                   COALESCE(reviewData.reviewCount, 0) as reviewCount,
                   CASE WHEN flashSale.id IS NOT NULL THEN true ELSE false END as isInFlashSale,
                   flashSale.discountPrice as flashSalePrice,
                   flashSale.stockQuantity as flashSaleStockQuantity,
                   COALESCE(flashSaleSold.soldCount, 0) as flashSaleSoldCount,
                   b.images as images
            FROM Book b
            LEFT JOIN (
                SELECT od.book.id as bookId,
                       SUM(od.quantity) as soldCount,
                       COUNT(DISTINCT od.order.id) as orderCount
                FROM OrderDetail od
                WHERE od.order.createdAt >= :thirtyDaysAgo
                      AND od.order.orderStatus = 'COMPLETED'
                GROUP BY od.book.id
            ) salesData ON b.id = salesData.bookId
            LEFT JOIN (
                SELECT r.book.id as bookId,
                       AVG(CAST(r.rating as double)) as avgRating,
                       COUNT(r.id) as reviewCount
                FROM Review r
                WHERE r.reviewStatus = 'APPROVED'
                      AND r.createdAt >= :sixtyDaysAgo
                GROUP BY r.book.id
            ) reviewData ON b.id = reviewData.bookId
            LEFT JOIN (
                SELECT fsi.book.id as bookId,
                       fsi.id as id,
                       fsi.discountPrice as discountPrice,
                       fsi.stockQuantity as stockQuantity
                FROM FlashSaleItem fsi
                JOIN FlashSale fs ON fsi.flashSale.id = fs.id
                WHERE fs.status = 1
                      AND fsi.status = 1
                      AND fs.startTime <= :currentTime
                      AND fs.endTime >= :currentTime
            ) flashSale ON b.id = flashSale.bookId
            LEFT JOIN (
                SELECT od.book.id as bookId,
                       SUM(od.quantity) as soldCount
                FROM OrderDetail od
                JOIN FlashSaleItem fsi ON od.book.id = fsi.book.id
                JOIN FlashSale fs ON fsi.flashSale.id = fs.id
                WHERE od.order.orderStatus = 'COMPLETED'
                      AND od.createdAt >= fs.startTime
                      AND od.createdAt <= fs.endTime
                      AND fs.status = 1
                      AND fsi.status = 1
                GROUP BY od.book.id
            ) flashSaleSold ON b.id = flashSaleSold.bookId
            WHERE b.status = 1
                  AND b.stockQuantity > 0
            ORDER BY (
                (COALESCE(salesData.soldCount, 0) * 0.4) +
                (COALESCE(reviewData.avgRating, 0) * COALESCE(reviewData.reviewCount, 0) * 0.3) +
                (CASE WHEN b.createdAt >= :thirtyDaysAgo THEN 10 ELSE 0 END * 0.2) +
                (CASE WHEN flashSale.id IS NOT NULL THEN 10 ELSE 0 END * 0.1)
            ) DESC
            """)
    Page<Object[]> findTrendingBooksData(
            @Param("thirtyDaysAgo") Long thirtyDaysAgo,
            @Param("sixtyDaysAgo") Long sixtyDaysAgo,
            @Param("currentTime") Long currentTime,
            Pageable pageable);

    /**
     * Đếm tổng số sách đủ điều kiện trending
     */
    @Query("""
            SELECT COUNT(DISTINCT b.id)
            FROM Book b
            WHERE b.status = 1
                  AND b.stockQuantity > 0
                  AND (:categoryId IS NULL OR b.category.id = :categoryId)
                  AND (:minPrice IS NULL OR b.price >= :minPrice)
                  AND (:maxPrice IS NULL OR b.price <= :maxPrice)
            """)
    Long countTrendingBooks(
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    /**
     * FALLBACK: Lấy sách theo thuật toán dự phòng khi chưa có đủ dữ liệu trending
     * Ưu tiên: Sách mới → Giá tốt → Stock nhiều → Ngẫu nhiên
     */
    @Query("""
            SELECT b.id as bookId,
                   b.bookName as bookName,
                   b.description as description,
                   b.price as price,
                   b.stockQuantity as stockQuantity,
                   b.bookCode as bookCode,
                   b.publicationDate as publicationDate,
                   b.createdAt as createdAt,
                   b.updatedAt as updatedAt,
                   b.category.id as categoryId,
                   b.category.categoryName as categoryName,
                   b.supplier.id as supplierId,
                   b.supplier.supplierName as supplierName,
                   0 as soldCount,
                   0 as orderCount,
                   0.0 as avgRating,
                   0 as reviewCount,
                   false as isInFlashSale,
                   NULL as flashSalePrice,
                   NULL as flashSaleStockQuantity,
                   0 as flashSaleSoldCount,
                   b.images as images
            FROM Book b
            WHERE b.status = 1
                  AND b.stockQuantity > 0
            ORDER BY
                b.createdAt DESC,
                b.price ASC,
                b.stockQuantity DESC,
                b.id DESC
            """)
    List<Object[]> findFallbackTrendingBooks(
            Pageable pageable);

    /**
     * Đếm tổng số sách active
     */
    @Query("""
            SELECT COUNT(b.id)
            FROM Book b
            WHERE b.status = 1
                  AND b.stockQuantity > 0
                  AND (:categoryId IS NULL OR b.category.id = :categoryId)
                  AND (:minPrice IS NULL OR b.price >= :minPrice)
                  AND (:maxPrice IS NULL OR b.price <= :maxPrice)
            """)
    Long countActiveBooks(
            @Param("categoryId") Integer categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Đếm tổng số sách active (không filter)
     */
    @Query("""
            SELECT COUNT(b.id)
            FROM Book b
            WHERE b.status = 1
                  AND b.stockQuantity > 0
            """)
    Long countAllActiveBooks();

    /**
     * HOT DISCOUNT: Lấy sách hot giảm sốc (flash sale + discount cao)
     */
    @Query("""
            SELECT b.id as bookId,
                   b.bookName as bookName,
                   b.description as description,
                   b.price as price,
                   b.stockQuantity as stockQuantity,
                   b.bookCode as bookCode,
                   b.publicationDate as publicationDate,
                   b.createdAt as createdAt,
                   b.updatedAt as updatedAt,
                   b.category.id as categoryId,
                   b.category.categoryName as categoryName,
                   b.supplier.id as supplierId,
                   b.supplier.supplierName as supplierName,
                   COALESCE(salesData.soldCount, 0) as soldCount,
                   COALESCE(salesData.orderCount, 0) as orderCount,
                   COALESCE(reviewData.avgRating, 0.0) as avgRating,
                   COALESCE(reviewData.reviewCount, 0) as reviewCount,
                   CASE WHEN flashSale.id IS NOT NULL THEN true ELSE false END as isInFlashSale,
                   flashSale.discountPrice as flashSalePrice,
                   flashSale.stockQuantity as flashSaleStockQuantity,
                   COALESCE(flashSaleSold.soldCount, 0) as flashSaleSoldCount,
                   b.images as images
            FROM Book b
            LEFT JOIN (
                SELECT od.book.id as bookId,
                       SUM(od.quantity) as soldCount,
                       COUNT(DISTINCT od.order.id) as orderCount
                FROM OrderDetail od
                WHERE od.order.orderStatus = 'COMPLETED'
                GROUP BY od.book.id
            ) salesData ON b.id = salesData.bookId
            LEFT JOIN (
                SELECT r.book.id as bookId,
                       AVG(CAST(r.rating as double)) as avgRating,
                       COUNT(r.id) as reviewCount
                FROM Review r
                WHERE r.reviewStatus = 'APPROVED'
                GROUP BY r.book.id
            ) reviewData ON b.id = reviewData.bookId
            LEFT JOIN (
                SELECT fsi.book.id as bookId,
                       fsi.id as id,
                       fsi.discountPrice as discountPrice,
                       fsi.stockQuantity as stockQuantity
                FROM FlashSaleItem fsi
                JOIN FlashSale fs ON fsi.flashSale.id = fs.id
                WHERE fs.status = 1
                      AND fsi.status = 1
                      AND fs.startTime <= :currentTime
                      AND fs.endTime >= :currentTime
            ) flashSale ON b.id = flashSale.bookId
            LEFT JOIN (
                SELECT od.book.id as bookId,
                       SUM(od.quantity) as soldCount
                FROM OrderDetail od
                JOIN FlashSaleItem fsi ON od.book.id = fsi.book.id
                JOIN FlashSale fs ON fsi.flashSale.id = fs.id
                WHERE od.order.orderStatus = 'COMPLETED'
                      AND od.createdAt >= fs.startTime
                      AND od.createdAt <= fs.endTime
                      AND fs.status = 1
                      AND fsi.status = 1
                GROUP BY od.book.id
            ) flashSaleSold ON b.id = flashSaleSold.bookId
            WHERE b.status = 1
                  AND b.stockQuantity > 0
                  AND (
                      flashSale.id IS NOT NULL OR
                      (b.discountActive = true AND (b.discountValue > 0 OR b.discountPercent > 0)) OR
                      (flashSale.id IS NOT NULL AND ((b.price - flashSale.discountPrice) / b.price * 100) >= 50)
                  )
            ORDER BY (
                (CASE WHEN flashSale.id IS NOT NULL THEN 20 ELSE 0 END) +
                (CASE WHEN (b.discountActive = true AND (b.discountValue > 0 OR b.discountPercent > 0)) THEN 15 ELSE 0 END) +
                COALESCE(reviewData.avgRating, 0)
            ) DESC
            """)
    Page<Object[]> findHotDiscountBooks(
            @Param("currentTime") Long currentTime,
            Pageable pageable);

    /**
     * FALLBACK: Lấy sách có giá tốt (cho hot discount fallback)
     */
    @Query("""
            SELECT b.id as bookId,
                   b.bookName as bookName,
                   b.description as description,
                   b.price as price,
                   b.stockQuantity as stockQuantity,
                   b.bookCode as bookCode,
                   b.publicationDate as publicationDate,
                   b.createdAt as createdAt,
                   b.updatedAt as updatedAt,
                   b.category.id as categoryId,
                   b.category.categoryName as categoryName,
                   b.supplier.id as supplierId,
                   b.supplier.supplierName as supplierName,
                   0 as soldCount,
                   0 as orderCount,
                   0.0 as avgRating,
                   0 as reviewCount,
                   false as isInFlashSale,
                   NULL as flashSalePrice,
                   NULL as flashSaleStockQuantity,
                   b.images as images
            FROM Book b
            WHERE b.status = 1
                  AND b.stockQuantity > 0
            ORDER BY
                b.price ASC,
                b.stockQuantity DESC,
                b.createdAt DESC
            """)
    List<Object[]> findGoodPriceBooks(
            Pageable pageable);

    /**
     * Tìm sách đang trong flash sale
     */
    @Query("""
            SELECT b.id as bookId,
                   b.bookName as bookName,
                   b.description as description,
                   fsi.discountPrice as price,
                   fsi.stockQuantity as stockQuantity,
                   b.bookCode as bookCode,
                   b.publicationDate as publicationDate,
                   b.createdAt as createdAt,
                   b.updatedAt as updatedAt,
                   b.category.id as categoryId,
                   b.category.categoryName as categoryName,
                   b.supplier.id as supplierId,
                   b.supplier.supplierName as supplierName,
                   COALESCE(sold.soldCount, 0) as soldCount,
                   COALESCE(sales.orderCount, 0) as orderCount,
                   COALESCE(reviews.avgRating, 0.0) as avgRating,
                   COALESCE(reviews.reviewCount, 0) as reviewCount
            FROM Book b
            JOIN FlashSaleItem fsi ON b.id = fsi.book.id
            JOIN FlashSale fs ON fsi.flashSale.id = fs.id
            LEFT JOIN (
                SELECT od.book.id as bookId, SUM(od.quantity) as soldCount
                FROM OrderDetail od
                WHERE od.order.orderStatus = 'COMPLETED'
                GROUP BY od.book.id
            ) sold ON b.id = sold.bookId
            LEFT JOIN (
                SELECT od.book.id as bookId, COUNT(DISTINCT od.order.id) as orderCount
                FROM OrderDetail od
                WHERE od.order.orderStatus = 'COMPLETED'
                GROUP BY od.book.id
            ) sales ON b.id = sales.bookId
            LEFT JOIN (
                SELECT r.book.id as bookId,
                       AVG(CAST(r.rating as double)) as avgRating,
                       COUNT(r.id) as reviewCount
                FROM Review r
                WHERE r.reviewStatus = 'APPROVED'
                GROUP BY r.book.id
            ) reviews ON b.id = reviews.bookId
            WHERE b.status = 1
                  AND fsi.status = 1
                  AND fs.status = 1
                  AND fs.startTime <= :currentTime
                  AND fs.endTime >= :currentTime
                  AND fsi.stockQuantity > 0
            ORDER BY
                (COALESCE(reviews.avgRating, 0) * 2 + COALESCE(sold.soldCount, 0) * 0.1) DESC,
                fsi.discountPrice ASC
            """)
    List<Object[]> findActiveFlashSaleBooks(
            @Param("currentTime") Long currentTime,
            Pageable pageable);

    @Query("""
            SELECT new org.datn.bookstation.dto.request.BookFlashSalesRequest(
                b.id,
                b.bookName,
                            b.price,b.stockQuantity

            )
            FROM Book b
            LEFT JOIN FlashSaleItem fsi ON b.id = fsi.book.id
            WHERE fsi.book.id IS NULL
            and
             b.status = 1
              AND b.stockQuantity > 0

            ORDER BY b.bookName ASC
            """)
    List<BookFlashSalesRequest> findActiveBooksWithStock();

    @Query("""
            SELECT new org.datn.bookstation.dto.request.BookFlashSalesRequest(
                b.id,
                b.bookName,
                            b.price,b.stockQuantity

            )
            FROM Book b
                        where
              b.stockQuantity > 0

            ORDER BY b.bookName ASC
            """)
    List<BookFlashSalesRequest> findActiveBooksForEdit();

    /**
     * Tìm kiếm sách active theo tên hoặc mã sách
     */
    @Query("SELECT b FROM Book b WHERE b.status = 1 AND " +
            "(UPPER(b.bookName) LIKE UPPER(CONCAT('%', :search, '%')) OR " +
            "UPPER(b.bookCode) LIKE UPPER(CONCAT('%', :search, '%'))) " +
            "ORDER BY b.createdAt DESC")
    List<Book> findActiveBooksByNameOrCode(@Param("search") String search);

    boolean existsByCategoryId(Integer id);

    boolean existsByAuthorBooks_Author_Id(Integer authorId);

    @Query("SELECT COALESCE(SUM(od.quantity), 0) FROM OrderDetail od WHERE od.order.orderStatus = 'COMPLETED'")
    Long getTotalSoldBooks();

    @Query("SELECT COALESCE(SUM(b.stockQuantity), 0) FROM Book b WHERE b.status = 1")
    Long getTotalStockBooks();

    @Query(value = """
            WITH revenue_calc AS (
                SELECT
                    COALESCE(SUM((o.total_amount - COALESCE(o.shipping_fee, 0)) * ((od.unit_price * od.quantity) / o.subtotal)), 0) -
                    COALESCE(SUM((o.total_amount - COALESCE(o.shipping_fee, 0)) * ((refunds.refund_quantity * od.unit_price) / o.subtotal)), 0) as netRevenue
                FROM order_detail od
                JOIN [order] o ON od.order_id = o.id
                LEFT JOIN (
                    SELECT rr.order_id, ri.book_id, SUM(ri.refund_quantity) as refund_quantity
                    FROM refund_item ri
                    JOIN refund_request rr ON ri.refund_request_id = rr.id
                    WHERE rr.status = 'COMPLETED'
                    GROUP BY rr.order_id, ri.book_id
                ) refunds ON od.order_id = refunds.order_id AND od.book_id = refunds.book_id
                WHERE o.order_status IN ('DELIVERED', 'REFUND_REQUESTED', 'AWAITING_GOODS_RETURN', 'GOODS_RECEIVED_FROM_CUSTOMER', 'GOODS_RETURNED_TO_WAREHOUSE', 'PARTIALLY_REFUNDED')
            )
            SELECT COALESCE(netRevenue, 0) FROM revenue_calc
            """, nativeQuery = true)
    BigDecimal getTotalRevenue();

    @Query(value = """
        SELECT b.book_name,
               (
                   COALESCE(SUM(od.quantity), 0) -
                   COALESCE(SUM(refunds.refund_quantity), 0)
               ) as net_quantity
        FROM order_detail od
        JOIN book b ON od.book_id = b.id
        JOIN [order] o ON od.order_id = o.id
        LEFT JOIN (
            SELECT rr.order_id, ri.book_id, SUM(ri.refund_quantity) as refund_quantity
            FROM refund_request rr
            JOIN refund_item ri ON rr.id = ri.refund_request_id
            WHERE rr.status = 'COMPLETED'
            GROUP BY rr.order_id, ri.book_id
        ) refunds ON od.order_id = refunds.order_id AND od.book_id = refunds.book_id
        WHERE o.order_status IN ('DELIVERED', 'REFUND_REQUESTED', 'PARTIALLY_REFUNDED')
        AND b.status = 1
        GROUP BY b.id, b.book_name
        HAVING (
            COALESCE(SUM(od.quantity), 0) -
            COALESCE(SUM(refunds.refund_quantity), 0)
        ) > 0
        ORDER BY net_quantity DESC
        """, nativeQuery = true)
List<Object[]> findTopBookSold(Pageable pageable);

    @Query("""
                SELECT new org.datn.bookstation.dto.response.BookStockResponse(
                    b.bookName,
                    b.stockQuantity
                )
                FROM Book b
                WHERE b.status = 1
                ORDER BY b.stockQuantity DESC
            """)
    List<BookStockResponse> findAllBookStock();

    Optional<Book> findByBookCodeIgnoreCase(String bookCode);
}
