package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.minigame.RewardRequest;
import org.datn.bookstation.dto.response.minigame.RewardResponse;
import org.datn.bookstation.dto.response.minigame.CampaignProbabilityResponse;

import java.util.List;

public interface RewardService {
    
    /**
     * Lấy danh sách phần thưởng của chiến dịch
     */
    List<RewardResponse> getRewardsByCampaign(Integer campaignId);
    
    /**
     * Tạo phần thưởng mới
     */
    void createReward(RewardRequest request);
    
    /**
     * Cập nhật phần thưởng
     */
    void updateReward(RewardRequest request);
    
    /**
     * Cập nhật trạng thái phần thưởng
     */
    void updateStatus(Integer id, Byte status, Integer updatedBy);
    void toggleStatus(Integer id, Integer updatedBy);
    
    /**
     * Xóa phần thưởng
     */
    void deleteReward(Integer id);
    
    /**
     * Lấy phần thưởng theo ID
     */
    RewardResponse getRewardById(Integer id);
    
    /**
     * Validate tổng xác suất của tất cả phần thưởng trong campaign = 100%
     */
    void validateTotalProbability(Integer campaignId, Integer excludeRewardId);
    
    /**
     * Lấy thông tin xác suất của chiến dịch
     */
    CampaignProbabilityResponse getCampaignProbabilityInfo(Integer campaignId);
}
