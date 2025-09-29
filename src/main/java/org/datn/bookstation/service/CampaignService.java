package org.datn.bookstation.service;

import org.datn.bookstation.dto.request.minigame.CampaignRequest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.minigame.CampaignResponse;

import java.util.List;

public interface CampaignService {
    
    /**
     * Lấy danh sách chiến dịch với phân trang
     */
    PaginationResponse<CampaignResponse> getAllWithPagination(int page, int size, String name, Byte status);
    
    /**
     * Lấy danh sách chiến dịch đang hoạt động cho user
     */
    List<CampaignResponse> getActiveCampaigns();
    
    /**
     * Lấy thông tin chi tiết chiến dịch
     */
    CampaignResponse getCampaignById(Integer id, Integer userId);
    
    /**
     * Tạo chiến dịch mới
     */
    void createCampaign(CampaignRequest request);
    
    /**
     * Cập nhật chiến dịch
     */
    void updateCampaign(CampaignRequest request);
    
    /**
     * Cập nhật trạng thái chiến dịch
     */
    void updateStatus(Integer id, Byte status, Integer updatedBy);
    void toggleStatus(Integer id, Integer updatedBy);
    
    /**
     * Xóa chiến dịch
     */
    void deleteCampaign(Integer id);
    
    /**
     * Kiểm tra chiến dịch có đang hoạt động không
     */
    boolean isCampaignActive(Integer campaignId);
}
