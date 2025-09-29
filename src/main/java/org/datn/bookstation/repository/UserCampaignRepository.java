package org.datn.bookstation.repository;

import org.datn.bookstation.entity.UserCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserCampaignRepository extends JpaRepository<UserCampaign, Integer> {
    
    // Tìm UserCampaign theo userId và campaignId
    Optional<UserCampaign> findByUserIdAndCampaignId(Integer userId, Integer campaignId);
    
    // Lấy tất cả chiến dịch user đã tham gia
    List<UserCampaign> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    // Lấy tất cả user tham gia chiến dịch
    List<UserCampaign> findByCampaignIdOrderByTotalOpenedCountDesc(Integer campaignId);
    
    // Đếm số user tham gia chiến dịch
    Long countByCampaignId(Integer campaignId);
    
    // Tính tổng số lần mở của chiến dịch
    @Query("SELECT SUM(uc.totalOpenedCount) FROM UserCampaign uc WHERE uc.campaign.id = :campaignId")
    Long sumTotalOpenedCountByCampaignId(@Param("campaignId") Integer campaignId);
    
    // Top users mở nhiều nhất
    @Query("SELECT uc FROM UserCampaign uc WHERE uc.campaign.id = :campaignId ORDER BY uc.totalOpenedCount DESC")
    List<UserCampaign> findTopUsersByCampaignId(@Param("campaignId") Integer campaignId);
}
