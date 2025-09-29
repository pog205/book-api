package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Point;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Integer>, JpaSpecificationExecutor<Point> {

    Point getByUserId(Integer userId);

    //  THÊM MỚI: Tổng điểm kiếm được trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(p.pointEarned), 0) FROM Point p WHERE p.createdAt BETWEEN :startTime AND :endTime AND p.pointEarned IS NOT NULL")
    Long countPointsEarnedInPeriod(@Param("startTime") long startTime, @Param("endTime") long endTime);

    //  THÊM MỚI: Tổng điểm tiêu trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(p.pointSpent), 0) FROM Point p WHERE p.createdAt BETWEEN :startTime AND :endTime AND p.pointSpent IS NOT NULL")
    Long countPointsSpentInPeriod(@Param("startTime") long startTime, @Param("endTime") long endTime);

    //  THÊM MỚI: Top người kiếm điểm nhiều nhất
    @Query("""
            SELECT u.fullName, u.email, 
                   COALESCE(SUM(p.pointEarned), 0) as totalEarned,
                   (SELECT r.rankName FROM UserRank ur 
                    JOIN ur.rank r WHERE ur.user = u AND ur.status = 1 
                    ORDER BY r.minSpent DESC LIMIT 1)
            FROM Point p 
            JOIN p.user u 
            WHERE p.pointEarned IS NOT NULL 
            GROUP BY u.id, u.fullName, u.email 
            ORDER BY totalEarned DESC
            """)
    List<Object[]> getTopPointEarners(Pageable pageable);
}
