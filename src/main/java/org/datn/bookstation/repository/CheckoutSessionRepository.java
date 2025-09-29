package org.datn.bookstation.repository;

import org.datn.bookstation.entity.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Integer>, JpaSpecificationExecutor<CheckoutSession> {
    
    /**
     * Tìm checkout session mới nhất của user (đang active)
     */
    @Query("SELECT cs FROM CheckoutSession cs WHERE cs.user.id = :userId AND cs.status = 1 " +
           "AND cs.expiresAt > :currentTime ORDER BY cs.createdAt DESC")
    List<CheckoutSession> findLatestActiveByUserId(@Param("userId") Integer userId, @Param("currentTime") Long currentTime);

    /**
     * Tìm tất cả checkout sessions của user (active)
     */
    @Query("SELECT cs FROM CheckoutSession cs WHERE cs.user.id = :userId AND cs.status = 1 " +
           "AND cs.expiresAt > :currentTime ORDER BY cs.createdAt DESC")
    List<CheckoutSession> findActiveByUserId(@Param("userId") Integer userId, @Param("currentTime") Long currentTime);

    /**
     * Tìm tất cả checkout sessions của user (bao gồm expired)
     */
    @Query("SELECT cs FROM CheckoutSession cs WHERE cs.user.id = :userId ORDER BY cs.createdAt DESC")
    List<CheckoutSession> findAllByUserId(@Param("userId") Integer userId);

    /**
     * Cập nhật status cho các sessions hết hạn
     */
    @Query("UPDATE CheckoutSession cs SET cs.status = 0, cs.updatedAt = :currentTime " +
           "WHERE cs.status = 1 AND cs.expiresAt <= :currentTime")
    int markExpiredSessions(@Param("currentTime") Long currentTime);

    /**
     * Xóa các session đã hết hạn lâu
     */
    @Modifying
    @Query("DELETE FROM CheckoutSession cs WHERE cs.status = 0 AND cs.expiresAt < :cutoffTime")
    int deleteOldExpiredSessions(@Param("cutoffTime") Long cutoffTime);

    /**
     * Đặt trạng thái tất cả các session của user (ngoại trừ status = 2) thành hết hạn ngay lập tức
     */
    @Modifying
    @Query("UPDATE CheckoutSession cs SET cs.status = 0, cs.expiresAt = :currentTime, cs.updatedAt = :currentTime " +
           "WHERE cs.user.id = :userId AND cs.status <> 2")
    int expireSessionsExceptCompleted(@Param("userId") Integer userId, @Param("currentTime") Long currentTime);

    /**
     * Đếm số session active của user
     */
    @Query("SELECT COUNT(cs) FROM CheckoutSession cs WHERE cs.user.id = :userId AND cs.status = 1 " +
           "AND cs.expiresAt > :currentTime")
    int countActiveSessionsByUserId(@Param("userId") Integer userId, @Param("currentTime") Long currentTime);

    /**
     * Tìm session theo ID và user (bảo mật)
     */
    @Query("SELECT cs FROM CheckoutSession cs WHERE cs.id = :sessionId AND cs.user.id = :userId")
    Optional<CheckoutSession> findByIdAndUserId(@Param("sessionId") Integer sessionId, @Param("userId") Integer userId);

    
}
