package org.datn.bookstation.repository;

import jakarta.validation.constraints.Size;
import org.datn.bookstation.dto.request.UserRoleRequest;
import org.datn.bookstation.dto.response.TopSpenderResponse;
import org.datn.bookstation.dto.response.UserResponse;
import org.datn.bookstation.entity.User;
import org.datn.bookstation.entity.enums.RoleName;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    List<User> findByRole_RoleName(RoleName roleName);

    @Query("""
            select new org.datn.bookstation.dto.request.UserRoleRequest(u.id, u.fullName, u.phoneNumber, u.role.id)
            from User u
            where u.role.id = 3
            and (
               lower(u.fullName) like lower(concat('%', :text, '%'))
                or u.phoneNumber like concat('%', :text, '%')
            )
            """)
    List<UserRoleRequest> getUserByIdRole(@Param("text") String text);

    User getByPhoneNumber(@Size(max = 20) String phoneNumber);

    /**
     * THÊM MỚI: Tìm kiếm khách hàng theo tên hoặc email
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(:searchTerm) OR " +
            "LOWER(u.email) LIKE LOWER(:searchTerm)")
    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            @Param("searchTerm") String searchTerm,
            @Param("searchTerm") String searchTerm2);

    @Query(value = """
            WITH user_revenue AS (
                SELECT
                    o.user_id,
                    COALESCE(SUM(o.total_amount), 0) - COALESCE(SUM(refunds.total_refund_amount), 0) as actualSpent
                FROM [order] o
                LEFT JOIN (
                    SELECT rr.order_id, SUM(rr.total_refund_amount) as total_refund_amount
                    FROM refund_request rr
                    WHERE rr.status = 'COMPLETED'
                    GROUP BY rr.order_id
                ) refunds ON o.id = refunds.order_id
                WHERE o.order_status IN ('DELIVERED', 'REFUND_REQUESTED', 'PARTIALLY_REFUNDED')
                AND o.user_id IS NOT NULL
                GROUP BY o.user_id
            )
            SELECT TOP 5
                u.full_name,
                COALESCE(ur.actualSpent, 0) as actualSpent,
                COALESCE((SELECT TOP 1 r.rank_name
                          FROM user_rank urk
                          JOIN rank r ON r.id = urk.rank_id
                          WHERE urk.user_id = u.id AND urk.status = 1
                          ORDER BY r.min_spent DESC), 'No Rank') as rankName
            FROM [user] u
            LEFT JOIN user_revenue ur ON u.id = ur.user_id
            WHERE u.role_id = 3
            ORDER BY COALESCE(ur.actualSpent, 0) DESC
            """, nativeQuery = true)
    List<TopSpenderResponse> findTopSpenders();

    long count();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1")
    long countActiveUsers();

    // THÊM MỚI: Đếm user mới trong khoảng thời gian
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startTime AND :endTime")
    long countByCreatedAtBetween(@Param("startTime") long startTime, @Param("endTime") long endTime);

    // THÊM MỚI: Đếm user đã mua hàng
    @Query("SELECT COUNT(DISTINCT o.createdBy) FROM Order o WHERE o.createdBy IS NOT NULL")
    long countUsersWithOrders();

    // THÊM MỚI: Điểm trung bình mỗi user
    @Query("SELECT AVG(u.totalPoint) FROM User u WHERE u.totalPoint IS NOT NULL")
    Double getAveragePointsPerUser();

    // THÊM MỚI: Tổng điểm toàn hệ thống
    @Query("SELECT COALESCE(SUM(u.totalPoint), 0) FROM User u")
    Long getTotalSystemPoints();

    // THÊM MỚI: Top user theo điểm
    @Query(value = """
            SELECT TOP 10 u.full_name, u.email, u.total_point,
                   (SELECT TOP 1 r.rank_name FROM user_rank ur
                    JOIN rank r ON r.id = ur.rank_id
                    WHERE ur.user_id = u.id AND ur.status = 1
                    ORDER BY r.min_spent DESC)
            FROM [user] u
            WHERE u.total_point IS NOT NULL
            ORDER BY u.total_point DESC
            """, nativeQuery = true)
    List<Object[]> getTopUsersByPoint();
}
