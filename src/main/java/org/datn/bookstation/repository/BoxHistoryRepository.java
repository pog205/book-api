package org.datn.bookstation.repository;

import org.datn.bookstation.entity.BoxHistory;
import org.datn.bookstation.entity.enums.BoxOpenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoxHistoryRepository extends JpaRepository<BoxHistory, Integer> {
    
    // Lấy lịch sử mở hộp của user
    List<BoxHistory> findByUserIdOrderByOpenDateDesc(Integer userId);
    
    // Lấy lịch sử mở hộp của user trong chiến dịch
    List<BoxHistory> findByUserIdAndCampaignIdOrderByOpenDateDesc(Integer userId, Integer campaignId);
    
    // Lấy lịch sử mở hộp của chiến dịch
    List<BoxHistory> findByCampaignIdOrderByOpenDateDesc(Integer campaignId);
    
    // Đếm số lần mở theo loại
    Long countByUserIdAndCampaignIdAndOpenType(Integer userId, Integer campaignId, BoxOpenType openType);
    
    // Thống kê theo ngày
    @Query("SELECT DATE(FROM_UNIXTIME(bh.openDate/1000)) as date, COUNT(bh) as count " +
           "FROM BoxHistory bh WHERE bh.campaign.id = :campaignId " +
           "GROUP BY DATE(FROM_UNIXTIME(bh.openDate/1000)) " +
           "ORDER BY date DESC")
    List<Object[]> getOpenStatsByCampaignId(@Param("campaignId") Integer campaignId);
    
    // Lấy lịch sử có phần thưởng
    @Query("SELECT bh FROM BoxHistory bh WHERE bh.user.id = :userId AND bh.reward IS NOT NULL ORDER BY bh.openDate DESC")
    List<BoxHistory> findUserWinningHistory(@Param("userId") Integer userId);
    
    // Thống kê tỷ lệ trúng của user
    @Query("SELECT COUNT(bh) FROM BoxHistory bh WHERE bh.user.id = :userId AND bh.campaign.id = :campaignId AND bh.reward IS NOT NULL")
    Long countWinsByUserAndCampaign(@Param("userId") Integer userId, @Param("campaignId") Integer campaignId);
    
    // Tổng số lần mở của user trong chiến dịch
    Long countByUserIdAndCampaignId(Integer userId, Integer campaignId);
    
    // ✅ Find box histories by reward ID
    List<BoxHistory> findByRewardId(Integer rewardId);
}
