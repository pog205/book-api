package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    /**
     * Tìm giỏ hàng của user
     */
    Optional<Cart> findByUserId(Integer userId);
    
    /**
     * Tìm giỏ hàng active của user
     */
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId AND c.status = 1")
    Optional<Cart> findActiveCartByUserId(@Param("userId") Integer userId);
    
    /**
     * Kiểm tra user có giỏ hàng không
     */
    boolean existsByUserId(Integer userId);
    
    /**
     * Đếm số items trong giỏ hàng
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.user.id = :userId AND ci.status = 1")
    Integer countItemsByUserId(@Param("userId") Integer userId);
}
