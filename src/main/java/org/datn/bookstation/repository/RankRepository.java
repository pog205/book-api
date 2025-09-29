package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Integer>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Rank> {
    boolean existsByRankName(String rankName);
    Optional<Rank> findByRankName(String rankName);

    //  THÊM MỚI: Thống kê user theo rank
    @Query("""
            SELECT r.rankName, 
                   (SELECT COUNT(ur.id) FROM UserRank ur WHERE ur.rank.id = r.id AND ur.status = 1),
                   r.minSpent
            FROM Rank r 
            ORDER BY r.minSpent ASC
            """)
    List<Object[]> getRankUserCounts();
}
