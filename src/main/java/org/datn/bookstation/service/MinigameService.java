package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.minigame.OpenBoxRequest;
import org.datn.bookstation.dto.response.minigame.OpenBoxResponse;
import org.datn.bookstation.dto.response.minigame.BoxHistoryResponse;
import org.datn.bookstation.dto.response.minigame.UserCampaignStatsResponse;

import java.util.List;

public interface MinigameService {
    
    /**
     * Mở hộp - Logic chính của minigame
     */
    OpenBoxResponse openBox(OpenBoxRequest request);
    
    /**
     * Lấy lịch sử mở hộp của user
     */
    List<BoxHistoryResponse> getUserHistory(Integer userId, Integer campaignId);
    
    /**
     * Lấy thống kê user trong chiến dịch
     */
    UserCampaignStatsResponse getUserCampaignStats(Integer userId, Integer campaignId);
    
    /**
     * Lấy lịch sử mở hộp của chiến dịch (cho admin)
     */
    List<BoxHistoryResponse> getCampaignHistory(Integer campaignId);
}
