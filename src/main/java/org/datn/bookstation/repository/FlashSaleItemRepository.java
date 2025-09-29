package org.datn.bookstation.repository;

import org.datn.bookstation.entity.FlashSaleItem;
import org.datn.bookstation.dto.request.FlashSaleItemBookRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FlashSaleItemRepository
                extends JpaRepository<FlashSaleItem, Integer>, JpaSpecificationExecutor<FlashSaleItem> {

        @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.flashSale.id = :flashSaleId")
        List<FlashSaleItem> findByFlashSaleId(@Param("flashSaleId") Integer flashSaleId);

        @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.book.id = :bookId")
        List<FlashSaleItem> findByBookId(@Param("bookId") Integer bookId);

        @Query("SELECT fsi FROM FlashSaleItem fsi WHERE fsi.flashSale.id = :flashSaleId AND fsi.status = 1")
        List<FlashSaleItem> findActiveByFlashSaleId(@Param("flashSaleId") Integer flashSaleId);

        boolean existsByFlashSaleIdAndBookId(Integer flashSaleId, Integer bookId);

        boolean existsByFlashSaleIdAndBookIdAndIdNot(Integer flashSaleId, Integer bookId, Integer id);

        /**
         * Lấy thông tin flash sale hiện tại của sách (đang active và trong thời gian
         * hiệu lực)
         */
        @Query("SELECT fsi FROM FlashSaleItem fsi " +
                        "WHERE fsi.book.id = :bookId " +
                        "AND fsi.status = 1 " +
                        "AND fsi.flashSale.status = 1 " +
                        "AND fsi.flashSale.startTime <= :currentTime " +
                        "AND fsi.flashSale.endTime >= :currentTime " +
                        "ORDER BY fsi.flashSale.startTime DESC")
        List<FlashSaleItem> findCurrentActiveFlashSaleByBookId(@Param("bookId") Integer bookId,
                        @Param("currentTime") Long currentTime);

        // ✅ REMOVED: Các query phức tạp tính soldCount - dùng trực tiếp field entity
        // countSoldQuantityByFlashSaleItem() và countUserPurchasedQuantity() đã bị xóa
        // Lý do: FlashSaleItem.soldCount sẽ được cập nhật trực tiếp khi thay đổi order
        // status

        // Bổ sung methods hỗ trợ Cart

        /**
         * Tìm flash sale đang active cho một sách (sử dụng Long timestamp)
         */
        @Query("SELECT fsi FROM FlashSaleItem fsi " +
                        "WHERE fsi.book.id = :bookId " +
                        "AND fsi.status = 1 " +
                        "AND fsi.flashSale.status = 1 " +
                        "AND fsi.flashSale.startTime <= :now " +
                        "AND fsi.flashSale.endTime >= :now " +
                        "ORDER BY fsi.discountPrice ASC")
        List<FlashSaleItem> findActiveFlashSalesByBookId(@Param("bookId") Long bookId, @Param("now") Long now);

        /**
         * Tìm flash sale item theo ID và kiểm tra còn active không
         */
        @Query("SELECT fsi FROM FlashSaleItem fsi " +
                        "WHERE fsi.id = :id " +
                        "AND fsi.status = 1 " +
                        "AND fsi.flashSale.status = 1 " +
                        "AND fsi.flashSale.startTime <= :now " +
                        "AND fsi.flashSale.endTime >= :now")
        Optional<FlashSaleItem> findActiveFlashSaleItemById(@Param("id") Long id, @Param("now") Long now);

        /**
         * Tìm flash sale item theo ID
         */
        Optional<FlashSaleItem> findById(Long id);

        /**
         * ✅ FIX LAZY LOADING: Lấy tất cả FlashSaleItem với FlashSale được fetch sẵn
         */
        @Query("SELECT fsi FROM FlashSaleItem fsi JOIN FETCH fsi.flashSale")
        List<FlashSaleItem> findAllWithFlashSale();

        /**
         * ✅ FIX LAZY LOADING: Lấy FlashSaleItem theo flashSaleId với FlashSale được
         * fetch sẵn
         */
        @Query("SELECT fsi FROM FlashSaleItem fsi JOIN FETCH fsi.flashSale WHERE fsi.flashSale.id = :flashSaleId")
        List<FlashSaleItem> findByFlashSaleIdWithFlashSale(@Param("flashSaleId") Integer flashSaleId);

        /**
         * ✅ ADMIN CẦN: Tìm Flash Sale đang active cho book (dùng cho
         * BookResponseMapper)
         */
        @Query("SELECT fsi FROM FlashSaleItem fsi " +
                        "WHERE fsi.book.id = :bookId " +
                        "AND fsi.status = 1 " +
                        "AND fsi.flashSale.status = 1 " +
                        "AND fsi.flashSale.startTime <= :currentTime " +
                        "AND fsi.flashSale.endTime >= :currentTime " +
                        "ORDER BY fsi.discountPrice ASC")
        FlashSaleItem findActiveFlashSaleByBook(@Param("bookId") Integer bookId,
                        @Param("currentTime") Long currentTime);

        /**
         * Helper method với current time tự động
         */
        default FlashSaleItem findActiveFlashSaleByBook(Integer bookId) {
                return findActiveFlashSaleByBook(bookId, System.currentTimeMillis());
        }

        /**
         * Lấy danh sách tất cả sách (Book) hiện đang trong flash‑sale còn hiệu lực.
         */
        @Query("""
                                SELECT b.id as id,
                                       b.bookName as bookName,
                                       b.price as price,
                                       b.stockQuantity as stockQuantity,
                                       b.images as images,
                                       b.category.categoryName as categoryName,
                                       CASE WHEN fsi.id IS NOT NULL THEN true ELSE false END as isInFlashSale,
                                       fsi.discountPrice as flashSalePrice,
                                       fsi.stockQuantity as flashSaleStockQuantity,
                                       COALESCE(flashSaleSold.soldCount, 0) as flashSaleSoldCount,
                                       COALESCE(salesData.soldCount, 0) as soldCount
                                FROM FlashSaleItem fsi
                                JOIN fsi.book b
                                JOIN b.category c
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
                                WHERE fsi.status = 1
                                  AND fsi.flashSale.status = 1
                                  AND fsi.flashSale.startTime <= :now
                                  AND fsi.flashSale.endTime >= :now
                                  AND b.status = 1
                                  AND b.stockQuantity > 0
                        """)
        List<Object[]> findAllBookFlashSaleDTO(@Param("now") Long now,
                        @Param("thirtyDaysAgo") Long thirtyDaysAgo);

        // Tổng số sách trong flash sale (đếm book_id duy nhất)
        @Query("SELECT COUNT(DISTINCT fsi.book.id) FROM FlashSaleItem fsi")
        long countTotalBooksInFlashSale();

        // Tổng số sách flash đã bán
        @Query("SELECT COALESCE(SUM(fsi.soldCount), 0) FROM FlashSaleItem fsi")
        long countTotalBooksSoldInFlashSale();

        // Tổng kho flash sale
        @Query("SELECT COALESCE(SUM(fsi.stockQuantity), 0) FROM FlashSaleItem fsi")
        long countTotalFlashSaleStock();

        // Top sách flash bán nhiều nhất (lấy tên sách)
        @Query("""
                        SELECT fsi.book.bookName FROM FlashSaleItem fsi
                        GROUP BY fsi.book.bookName
                        ORDER BY SUM(fsi.soldCount) DESC
                        """)
        List<String> findTopSellingBookName(Pageable pageable);
}
