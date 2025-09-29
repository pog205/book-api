package org.datn.bookstation.repository;

import org.datn.bookstation.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    
    /**
     * Lấy tất cả items trong giỏ hàng
     */
    List<CartItem> findByCartId(Integer cartId);
    
    /**
     * Lấy items active trong giỏ hàng
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 1")
    List<CartItem> findActiveItemsByCartId(@Param("cartId") Integer cartId);
    
    /**
     * Lấy items của user (qua cart)
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId AND ci.status = 1")
    List<CartItem> findByUserId(@Param("userId") Integer userId);
    
    /**
     * Tìm CartItem cụ thể trong giỏ hàng
     * - Cùng book và không có flash sale
     * - Hoặc cùng book và cùng flash sale item
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId " +
           "AND ci.book.id = :bookId " +
           "AND (:flashSaleItemId IS NULL AND ci.flashSaleItem IS NULL " +
           "     OR ci.flashSaleItem.id = :flashSaleItemId) " +
           "AND ci.status = 1")
    Optional<CartItem> findExistingCartItem(@Param("cartId") Integer cartId, 
                                          @Param("bookId") Integer bookId,
                                          @Param("flashSaleItemId") Integer flashSaleItemId);

    /**
     * 🔥 NEW: Tìm tất cả cart items của cùng book (để merge duplicates)
     * Sắp xếp theo updatedAt DESC để lấy item mới nhất trước
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId " +
           "AND ci.book.id = :bookId " +
           "AND ci.status = 1 " +
           "ORDER BY ci.updatedAt DESC")
    List<CartItem> findExistingCartItemsByBook(@Param("cartId") Integer cartId, 
                                              @Param("bookId") Integer bookId);

    /**
     * Xóa tất cả items trong giỏ hàng
     */
    void deleteByCartId(Integer cartId);
    
    /**
     * Đếm số items trong giỏ hàng
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 1")
    Integer countActiveItemsByCartId(@Param("cartId") Integer cartId);
    
    /**
     * Tính tổng quantity trong giỏ hàng
     */
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 1")
    Integer sumQuantityByCartId(@Param("cartId") Integer cartId);
    
    /**
     * Tìm items có flash sale hết hạn cho user cụ thể
     */
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE ci.cart.user.id = :userId " +
           "AND ci.flashSaleItem IS NOT NULL " +
           "AND ci.flashSaleItem.flashSale.endTime < :currentTime " +
           "AND ci.status = 1")
    List<CartItem> findExpiredFlashSaleItems(@Param("userId") Integer userId, 
                                           @Param("currentTime") Long currentTime);
    
    /**
     * Tìm TẤT CẢ cart items có flash sale đã hết hạn (cho scheduler batch processing)
     */
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE ci.flashSaleItem IS NOT NULL " +
           "AND ci.flashSaleItem.flashSale.endTime < :currentTime " +
           "AND ci.status = 1")
    List<CartItem> findAllExpiredFlashSaleItems(@Param("currentTime") Long currentTime);
    
    /**
     * Tìm cart items của một flash sale cụ thể (cho dynamic scheduler)
     */
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE ci.flashSaleItem.flashSale.id = :flashSaleId " +
           "AND ci.status = 1")
    List<CartItem> findByFlashSaleId(@Param("flashSaleId") Integer flashSaleId);
    
    /**
     * ✅ DEPRECATED: Không còn cần batch update cart items khi flash sale hết hạn
     * Logic mới: Giữ nguyên flashSaleItemId, chỉ update status của FlashSaleItem
     * Cart sẽ tự động trả về giá gốc khi flashSaleItem.status = 0
     */
    // @Deprecated - Removed to prevent accidentally setting flashSaleItem = null
    // int batchUpdateExpiredFlashSales(@Param("flashSaleIds") List<Integer> flashSaleIds, 
    //                                @Param("updatedAt") Long updatedAt);
    
    /**
     * Validate cart item stock before processing
     * Kiểm tra quantity trong cart có vượt quá stock hiện tại không
     */
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE ci.cart.user.id = :userId " +
           "AND ci.status = 1 " +
           "AND ((ci.flashSaleItem IS NOT NULL AND ci.quantity > ci.flashSaleItem.stockQuantity) " +
           "     OR (ci.flashSaleItem IS NULL AND ci.quantity > ci.book.stockQuantity))")
    List<CartItem> findCartItemsExceedingStock(@Param("userId") Integer userId);
    
    /**
     * Tìm cart items có flash sale sắp hết hạn (còn 5 phút)
     * Dùng để gửi notification warning cho user
     */
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE ci.flashSaleItem IS NOT NULL " +
           "AND ci.flashSaleItem.flashSale.endTime BETWEEN :now AND :warningTime " +
           "AND ci.status = 1")
    List<CartItem> findFlashSaleItemsAboutToExpire(@Param("now") Long now, 
                                                  @Param("warningTime") Long warningTime);

    /**
     * 🔄 NEW: Tìm cart items cần sync với flash sale mới được gia hạn
     * Tìm items của book có flash sale được update mà chưa apply flash sale này
     */
    @Query("SELECT ci FROM CartItem ci " +
           "WHERE ci.book.id = :bookId " +
           "AND ci.status = 1 " +
           "AND (ci.flashSaleItem IS NULL " +
           "     OR ci.flashSaleItem.flashSale.id != :flashSaleId)")
    List<CartItem> findCartItemsForFlashSaleSync(@Param("bookId") Long bookId,
                                                 @Param("flashSaleId") Integer flashSaleId);

    /**
     * 🔥 NEW: Tìm cart items của book mà chưa có flash sale item
     * Dùng để sync khi admin tạo flash sale mới cho sản phẩm đã có trong cart
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.book.id = :bookId " +
           "AND ci.flashSaleItem IS NULL " +
           "AND ci.status = 1")
    List<CartItem> findCartItemsWithoutFlashSale(@Param("bookId") Long bookId);
}
