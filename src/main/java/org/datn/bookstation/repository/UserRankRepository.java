package org.datn.bookstation.repository;

import org.datn.bookstation.entity.UserRank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRankRepository extends JpaRepository<UserRank, Integer>, JpaSpecificationExecutor<UserRank> {
    List<UserRank> findByRankId(Integer rankId);

    Page<UserRank> findByRankIdAndUserEmailContainingIgnoreCaseAndUserFullNameContainingIgnoreCase(
        Integer rankId, String email, String fullName, Pageable pageable);

    List<UserRank> getByUserId(Integer userId);

    //  THÊM MỚI: Số user theo từng rank
    @Query("""
            SELECT r.rankName, COUNT(ur.id) 
            FROM UserRank ur 
            JOIN ur.rank r 
            WHERE ur.status = 1 
            GROUP BY r.rankName
            """)
    List<Object[]> getUserCountByRank();

    //  THÊM MỚI: Điểm trung bình theo rank
    @Query("""
            SELECT r.rankName, AVG(u.totalPoint), r.minSpent
            FROM UserRank ur 
            JOIN ur.rank r 
            JOIN ur.user u 
            WHERE ur.status = 1 AND u.totalPoint IS NOT NULL 
            GROUP BY r.rankName, r.minSpent
            """)
    List<Object[]> getAveragePointsByRank();

    //  THÊM MỚI: Tỷ lệ tăng giảm theo tháng
    @Query("""
            SELECT r.rankName, 
                   (SELECT COUNT(ur2.id) FROM UserRank ur2 
                    JOIN ur2.rank r2 WHERE r2.id = r.id AND ur2.status = 1 
                    AND ur2.createdAt BETWEEN :currentStart AND :currentEnd),
                   (SELECT COUNT(ur3.id) FROM UserRank ur3 
                    JOIN ur3.rank r3 WHERE r3.id = r.id AND ur3.status = 1 
                    AND ur3.createdAt BETWEEN :previousStart AND :previousEnd)
            FROM Rank r
            """)
    List<Object[]> getMonthlyGrowthRates(@Param("currentStart") long currentStart, 
                                       @Param("currentEnd") long currentEnd,
                                       @Param("previousStart") long previousStart, 
                                       @Param("previousEnd") long previousEnd);
}
