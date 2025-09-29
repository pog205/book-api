package org.datn.bookstation.repository;

import org.datn.bookstation.entity.Reward;
import org.datn.bookstation.entity.enums.RewardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Integer> {
    
    // Lấy phần thưởng của chiến dịch đang hoạt động
    @Query("SELECT r FROM Reward r WHERE r.campaign.id = :campaignId AND r.status = 1 ORDER BY r.probability DESC")
    List<Reward> findActiveByCampaignId(@Param("campaignId") Integer campaignId);
    
    // Lấy phần thưởng còn số lượng  
    @Query("SELECT r FROM Reward r WHERE r.campaign.id = :campaignId AND r.status = 1 AND r.stock > 0 ORDER BY r.probability DESC")
    List<Reward> findAvailableByCampaignId(@Param("campaignId") Integer campaignId);
    
    // Lấy phần thưởng theo loại
    @Query("SELECT r FROM Reward r WHERE r.campaign.id = :campaignId AND r.status = 1 AND r.type = :type")
    List<Reward> findByCampaignIdAndType(@Param("campaignId") Integer campaignId, @Param("type") RewardType type);
    
    // TODO: Đếm tổng số phần thưởng đã phát - cần implement từ history table
    @Query("SELECT COUNT(*) FROM BoxHistory bh WHERE bh.campaign.id = :campaignId AND bh.reward IS NOT NULL")
    Long countDistributedByCampaignId(@Param("campaignId") Integer campaignId);
    
    // Lấy phần thưởng theo campaign
    List<Reward> findByCampaignIdOrderByProbabilityDesc(Integer campaignId);
}
