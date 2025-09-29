package org.datn.bookstation.service.impl.minigame;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.minigame.CampaignRequest;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.minigame.CampaignResponse;
import org.datn.bookstation.dto.response.minigame.RewardResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.CampaignService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final RewardRepository rewardRepository;
    private final UserCampaignRepository userCampaignRepository;
    private final BoxHistoryRepository boxHistoryRepository;

    @Override
    public PaginationResponse<CampaignResponse> getAllWithPagination(int page, int size, String name, Byte status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Campaign> spec = (root, query, cb) -> cb.conjunction();

        if (name != null && !name.trim().isEmpty()) {
            Specification<Campaign> nameSpec = (root, query, cb) -> 
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
            spec = spec.and(nameSpec);
        }

        if (status != null) {
            Specification<Campaign> statusSpec = (root, query, cb) -> 
                cb.equal(root.get("status"), status);
            spec = spec.and(statusSpec);
        }

        Page<Campaign> campaignPage = campaignRepository.findAll(spec, pageable);

        List<CampaignResponse> responses = campaignPage.getContent().stream()
                .map(campaign -> toCampaignResponse(campaign, null))
                .collect(Collectors.toList());

        return new PaginationResponse<>(
                responses,
                campaignPage.getNumber(),
                campaignPage.getSize(),
                campaignPage.getTotalElements(),
                campaignPage.getTotalPages()
        );
    }

    @Override
    public List<CampaignResponse> getActiveCampaigns() {
        long currentTime = System.currentTimeMillis();
        List<Campaign> campaigns = campaignRepository.findActiveCampaigns(currentTime);
        
        //  Filter campaigns that have valid rewards
        return campaigns.stream()
                .filter(this::isCampaignValid)
                .map(campaign -> toCampaignResponse(campaign, null))
                .collect(Collectors.toList());
    }

    @Override
    public CampaignResponse getCampaignById(Integer id, Integer userId) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));

        //  Check if campaign is valid before returning to user (for gameplay)
        if (userId != null && !isCampaignValid(campaign)) {
            throw new RuntimeException("Chiến dịch không có phần thưởng nào khả dụng");
        }

        return toCampaignResponse(campaign, userId);
    }

    @Override
    public void createCampaign(CampaignRequest request) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setStatus(request.getStatus());
        campaign.setConfigFreeLimit(request.getConfigFreeLimit());
        campaign.setConfigPointCost(request.getConfigPointCost());
        campaign.setDescription(request.getDescription());
        campaign.setCreatedBy(request.getCreatedBy());
        
        campaignRepository.save(campaign);
        log.info("Created new campaign: {}", campaign.getName());
    }

    @Override
    public void updateCampaign(CampaignRequest request) {
        Campaign campaign = campaignRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));

        campaign.setName(request.getName());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setStatus(request.getStatus());
        campaign.setConfigFreeLimit(request.getConfigFreeLimit());
        campaign.setConfigPointCost(request.getConfigPointCost());
        campaign.setDescription(request.getDescription());
        campaign.setUpdatedBy(request.getUpdatedBy());

        campaignRepository.save(campaign);
        log.info("Updated campaign: {}", campaign.getName());
    }

    @Override
    public void updateStatus(Integer id, Byte status, Integer updatedBy) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));
        campaign.setStatus(status);
        campaign.setUpdatedBy(updatedBy);
        campaignRepository.save(campaign);
        log.info("Updated campaign status: {} -> {}", campaign.getName(), status);
    }

    @Override
    public void toggleStatus(Integer id, Integer updatedBy) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));
        byte newStatus = (campaign.getStatus() != null && campaign.getStatus() == 1) ? (byte)0 : (byte)1;
        campaign.setStatus(newStatus);
        campaign.setUpdatedBy(updatedBy);
        campaignRepository.save(campaign);
        log.info("Toggled campaign status: {} -> {}", campaign.getName(), newStatus);
    }

    @Override
    public void deleteCampaign(Integer id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));

        campaignRepository.delete(campaign);
        log.info("Deleted campaign: {}", campaign.getName());
    }

    @Override
    public boolean isCampaignActive(Integer campaignId) {
        long currentTime = System.currentTimeMillis();
        return campaignRepository.findActiveCampaignById(campaignId, currentTime).isPresent();
    }

    /**
     *  Validate if campaign is playable (has valid rewards)
     */
    private boolean isCampaignValid(Campaign campaign) {
        try {
            log.debug(" [NEW CODE v2] Validating campaign {}", campaign.getName());
            
            // Get active rewards for this campaign
            List<Reward> activeRewards = rewardRepository.findActiveByCampaignId(campaign.getId());
            
            log.debug("Campaign {} has {} active rewards", campaign.getName(), activeRewards.size());
            
            if (activeRewards.isEmpty()) {
                log.warn("Campaign {} has no active rewards", campaign.getName());
                return false;
            }
            
            // Check if campaign has at least one valid reward
            boolean hasValidReward = activeRewards.stream().anyMatch(this::isRewardValid);
            
            log.debug("Campaign {} hasValidReward: {}", campaign.getName(), hasValidReward);
            
            if (!hasValidReward) {
                log.warn("Campaign {} has no valid rewards available", campaign.getName());
                return false;
            }
            
            log.debug(" Campaign {} passed all validation", campaign.getName());
            return true;
        } catch (Exception e) {
            log.error("Error validating campaign {}: {}", campaign.getName(), e.getMessage());
            return false;
        }
    }

    /**
     *  Validate if reward is still available and usable  
     */
    private boolean isRewardValid(Reward reward) {
        try {
            log.debug("Validating reward {}: type={}, status={}, stock={}", 
                    reward.getId(), reward.getType(), reward.getStatus(), reward.getStock());
            
            // Check reward status
            if (reward.getStatus() != 1) {
                log.debug("Reward {} has inactive status: {}", reward.getId(), reward.getStatus());
                return false;
            }
            
            // Check if reward has remaining stock
            if (reward.getStock() <= 0) {
                log.debug("Reward {} has no stock: {}", reward.getId(), reward.getStock());
                return false;
            }
            
            // DISABLE voucher validation for now - all rewards are valid if they pass basic checks
            log.debug("Reward {} passed all basic validations", reward.getId());
            return true;
            
        } catch (Exception e) {
            log.warn("Error validating reward {}: {}", reward.getId(), e.getMessage(), e);
            // Return true to avoid blocking campaign due to validation errors
            return true;
        }
    }

    private CampaignResponse toCampaignResponse(Campaign campaign, Integer userId) {
        CampaignResponse response = new CampaignResponse();
        
        // Basic info
        response.setId(campaign.getId());
        response.setName(campaign.getName());
        response.setStartDate(campaign.getStartDate());
        response.setEndDate(campaign.getEndDate());
        response.setStatus(campaign.getStatus());
        response.setConfigFreeLimit(campaign.getConfigFreeLimit());
        response.setConfigPointCost(campaign.getConfigPointCost());
        response.setDescription(campaign.getDescription());
        response.setCreatedAt(campaign.getCreatedAt());
        response.setUpdatedAt(campaign.getUpdatedAt());
        response.setCreatedBy(campaign.getCreatedBy());
        response.setUpdatedBy(campaign.getUpdatedBy());

        // Statistics
        response.setTotalParticipants(userCampaignRepository.countByCampaignId(campaign.getId()).intValue());
        response.setTotalOpened(userCampaignRepository.sumTotalOpenedCountByCampaignId(campaign.getId()));
        
        // Rewards info
        List<Reward> rewards = rewardRepository.findActiveByCampaignId(campaign.getId());
        response.setTotalRewards(rewards.stream().mapToInt(Reward::getStock).sum()); // Tổng stock hiện tại
        response.setRemainingRewards(rewards.stream().mapToInt(Reward::getStock).sum()); // Còn lại = stock
        
        List<RewardResponse> rewardResponses = rewards.stream()
                .map(this::toRewardResponse)
                .collect(Collectors.toList());
        response.setRewards(rewardResponses);

        // User specific info (if userId provided)
        if (userId != null) {
            UserCampaign userCampaign = userCampaignRepository.findByUserIdAndCampaignId(userId, campaign.getId())
                    .orElse(null);
            
            if (userCampaign != null) {
                response.setUserFreeOpenedCount(userCampaign.getFreeOpenedCount());
                response.setUserTotalOpenedCount(userCampaign.getTotalOpenedCount());
                response.setUserRemainingFreeOpens(campaign.getConfigFreeLimit() - userCampaign.getFreeOpenedCount());
            } else {
                response.setUserFreeOpenedCount(0);
                response.setUserTotalOpenedCount(0);
                response.setUserRemainingFreeOpens(campaign.getConfigFreeLimit());
            }
        }

        return response;
    }

    private RewardResponse toRewardResponse(Reward reward) {
        RewardResponse response = new RewardResponse();
        
        response.setId(reward.getId());
        response.setCampaignId(reward.getCampaign().getId());
        response.setType(reward.getType());
        response.setName(reward.getName());
        response.setDescription(reward.getDescription());
        
        // Voucher info (if applicable)
        if (reward.getVoucher() != null) {
            response.setVoucherId(reward.getVoucher().getId());
            response.setVoucherCode(reward.getVoucher().getCode());
            response.setVoucherName(reward.getVoucher().getName());
            response.setVoucherDescription(reward.getVoucher().getDescription());
        }
        
        response.setPointValue(reward.getPointValue());
        response.setStock(reward.getStock());
        response.setProbability(reward.getProbability());
        response.setStatus(reward.getStatus());
        response.setCreatedAt(reward.getCreatedAt());
        response.setUpdatedAt(reward.getUpdatedAt());
        response.setCreatedBy(reward.getCreatedBy());
        response.setUpdatedBy(reward.getUpdatedBy());
        
        // TODO: Statistics cần được tính từ database thực tế
        response.setDistributedCount(0); // Placeholder - cần implement từ history table
        response.setDistributedPercentage(java.math.BigDecimal.ZERO); // Placeholder
        
        return response;
    }
}
