package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Integer>, JpaSpecificationExecutor<Campaign> {
    
    // Tìm các chiến dịch đang hoạt động
    @Query("SELECT c FROM Campaign c WHERE c.status = 1 AND :currentTime BETWEEN c.startDate AND c.endDate ORDER BY c.createdAt DESC")
    List<Campaign> findActiveCampaigns(@Param("currentTime") Long currentTime);
    
    // Tìm tất cả chiến dịch đang hoạt động (không quan tâm thời gian)
    List<Campaign> findByStatusOrderByCreatedAtDesc(Byte status);
    
    // Tìm chiến dịch theo tên
    @Query("SELECT c FROM Campaign c WHERE c.name LIKE %:name% ORDER BY c.createdAt DESC")
    List<Campaign> findByNameContaining(@Param("name") String name);
    
    // Kiểm tra chiến dịch có đang hoạt động không
    @Query("SELECT c FROM Campaign c WHERE c.id = :id AND c.status = 1 AND :currentTime BETWEEN c.startDate AND c.endDate")
    Optional<Campaign> findActiveCampaignById(@Param("id") Integer id, @Param("currentTime") Long currentTime);
}
