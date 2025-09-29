package org.datn.bookstation.service.impl.minigame;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.minigame.RewardRequest;
import org.datn.bookstation.dto.response.minigame.RewardResponse;
import org.datn.bookstation.dto.response.minigame.CampaignProbabilityResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.RewardType;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.RewardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class RewardServiceImpl implements RewardService {

    private final RewardRepository rewardRepository;
    private final CampaignRepository campaignRepository;
    private final VoucherRepository voucherRepository;
    private final BoxHistoryRepository boxHistoryRepository;

    @Override
    public List<RewardResponse> getRewardsByCampaign(Integer campaignId) {
        List<Reward> rewards = rewardRepository.findByCampaignIdOrderByProbabilityDesc(campaignId);
        return rewards.stream()
                .map(this::toRewardResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void createReward(RewardRequest request) {
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));
        
        //  VALIDATION: Kiểm tra tổng xác suất trước khi tạo
        validateTotalProbabilityForCreate(request.getCampaignId(), request.getProbability());

        Reward reward = new Reward();
        reward.setCampaign(campaign);
        reward.setType(request.getType());
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        
        // Validate and set specific reward data based on type
        if (request.getType() == RewardType.VOUCHER) {
            if (request.getVoucherId() == null) {
                throw new RuntimeException("Voucher ID là bắt buộc cho phần thưởng voucher");
            }
            Voucher voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
            
            //  Validate voucher quantity availability
            if (voucher.getUsageLimit() != null && request.getStock() > voucher.getUsageLimit()) {
                throw new RuntimeException("Số lượng phần thưởng (" + request.getStock() + 
                                         ") không thể vượt quá số lượng có sẵn của voucher (" + 
                                         voucher.getUsageLimit() + ")");
            }
            
            //  Check remaining usage of voucher
            int remainingVoucherUsage = voucher.getUsageLimit() != null ? 
                                       voucher.getUsageLimit() - (voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) : 
                                       Integer.MAX_VALUE;
            if (request.getStock() > remainingVoucherUsage) {
                throw new RuntimeException("Số lượng phần thưởng (" + request.getStock() + 
                                         ") vượt quá số lượt sử dụng còn lại của voucher (" + 
                                         remainingVoucherUsage + ")");
            }
            
            reward.setVoucher(voucher);
        } else if (request.getType() == RewardType.POINTS) {
            if (request.getPointValue() == null || request.getPointValue() <= 0) {
                throw new RuntimeException("Giá trị điểm phải lớn hơn 0");
            }
            reward.setPointValue(request.getPointValue());
        }
        
        reward.setStock(request.getStock());
        reward.setProbability(request.getProbability());
        reward.setStatus(request.getStatus());
        reward.setCreatedBy(request.getCreatedBy());
        
        rewardRepository.save(reward);
        log.info("Created new reward: {} for campaign: {}", reward.getName(), campaign.getName());
    }

    @Override
    public void updateReward(RewardRequest request) {
        Reward reward = rewardRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));
        
        //  VALIDATION: Kiểm tra tổng xác suất trước khi update (exclude reward hiện tại)
        validateTotalProbabilityForUpdate(reward.getCampaign().getId(), request.getId(), request.getProbability());

        reward.setType(request.getType());
        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        
        // Update specific reward data based on type
        if (request.getType() == RewardType.VOUCHER) {
            if (request.getVoucherId() == null) {
                throw new RuntimeException("Voucher ID là bắt buộc cho phần thưởng voucher");
            }
            Voucher voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
            
            //  Validate voucher quantity availability for update
            if (voucher.getUsageLimit() != null && request.getStock() > voucher.getUsageLimit()) {
                throw new RuntimeException("Số lượng phần thưởng (" + request.getStock() + 
                                         ") không thể vượt quá số lượng có sẵn của voucher (" + 
                                         voucher.getUsageLimit() + ")");
            }
            
            //  Check remaining usage of voucher
            int remainingVoucherUsage = voucher.getUsageLimit() != null ? 
                                       voucher.getUsageLimit() - (voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) : 
                                       Integer.MAX_VALUE;
            if (request.getStock() > remainingVoucherUsage) {
                throw new RuntimeException("Số lượng phần thưởng (" + request.getStock() + 
                                         ") vượt quá số lượt sử dụng còn lại của voucher (" + 
                                         remainingVoucherUsage + ")");
            }
            
            reward.setVoucher(voucher);
            reward.setPointValue(null); // Clear point value
        } else if (request.getType() == RewardType.POINTS) {
            if (request.getPointValue() == null || request.getPointValue() <= 0) {
                throw new RuntimeException("Giá trị điểm phải lớn hơn 0");
            }
            reward.setPointValue(request.getPointValue());
            reward.setVoucher(null); // Clear voucher
        } else { // NONE
            reward.setVoucher(null);
            reward.setPointValue(null);
        }
        
        reward.setStock(request.getStock());
        reward.setProbability(request.getProbability());
        reward.setStatus(request.getStatus());
        reward.setUpdatedBy(request.getUpdatedBy());
        
        rewardRepository.save(reward);
        log.info("Updated reward: {}", reward.getName());
    }

    @Override
    public void updateStatus(Integer id, Byte status, Integer updatedBy) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));
        
        //  NEW: Validate khi bật phần thưởng (status = 1)
        if (status == 1 && (reward.getStatus() == null || reward.getStatus() != 1)) {
            validateActiveProbabilityWhenEnabling(reward.getCampaign().getId(), reward.getProbability());
        }
        
        reward.setStatus(status);
        reward.setUpdatedBy(updatedBy);
        rewardRepository.save(reward);
        log.info("Updated reward status: {} -> {}", reward.getName(), status);
    }

    @Override
    public void toggleStatus(Integer id, Integer updatedBy) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));
        byte newStatus = (reward.getStatus() != null && reward.getStatus() == 1) ? (byte)0 : (byte)1;
        
        //  NEW: Validate khi bật phần thưởng (newStatus = 1)
        if (newStatus == 1) {
            validateActiveProbabilityWhenEnabling(reward.getCampaign().getId(), reward.getProbability());
        }
        
        reward.setStatus(newStatus);
        reward.setUpdatedBy(updatedBy);
        rewardRepository.save(reward);
        log.info("Toggled reward status: {} -> {}", reward.getName(), newStatus);
    }

    @Override
    public void deleteReward(Integer id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));

        //  Check if reward is being used in box_history
        List<BoxHistory> boxHistories = boxHistoryRepository.findByRewardId(id);
        
        if (!boxHistories.isEmpty()) {
            //  Set reward_id to null in box_history before deleting reward
            for (BoxHistory history : boxHistories) {
                history.setReward(null);
                boxHistoryRepository.save(history);
            }
            log.info("Updated {} box history records to remove reward reference", boxHistories.size());
        }

        rewardRepository.delete(reward);
        log.info("Deleted reward: {}", reward.getName());
    }

    @Override
    public RewardResponse getRewardById(Integer id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phần thưởng không tồn tại"));

        return toRewardResponse(reward);
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
    
    //  NEW: Validation methods - Updated to use ACTIVE rewards only
    @Override
    public void validateTotalProbability(Integer campaignId, Integer excludeRewardId) {
        BigDecimal totalActiveProbability = calculateTotalActiveProbability(campaignId, excludeRewardId);
        //  CHANGED: New rule - total active probability should be <= 100%
        if (totalActiveProbability.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("Tổng xác suất của các phần thưởng đang hoạt động không được vượt quá 100%. Hiện tại: " + totalActiveProbability + "%");
        }
    }
    
    private void validateTotalProbabilityForCreate(Integer campaignId, BigDecimal newProbability) {
        BigDecimal totalActiveProbability = calculateTotalActiveProbability(campaignId, null);
        BigDecimal newTotal = totalActiveProbability.add(newProbability);
        if (newTotal.compareTo(new BigDecimal("100")) > 0) {
            BigDecimal remaining = new BigDecimal("100").subtract(totalActiveProbability);
            throw new RuntimeException("Không thể tạo phần thưởng với xác suất " + newProbability + "%. " +
                                     "Tổng xác suất các phần thưởng đang hoạt động: " + totalActiveProbability + "%, " +
                                     "Chỉ còn lại: " + remaining + "%. Quy tắc: chỉ tính phần thưởng active (status=1)");
        }
    }
    
    private void validateTotalProbabilityForUpdate(Integer campaignId, Integer rewardId, BigDecimal newProbability) {
        //  FIXED: Use active probability calculation only
        BigDecimal totalActiveProbability = calculateTotalActiveProbability(campaignId, rewardId);
        BigDecimal newTotal = totalActiveProbability.add(newProbability);
        if (newTotal.compareTo(new BigDecimal("100")) > 0) {
            BigDecimal remaining = new BigDecimal("100").subtract(totalActiveProbability);
            throw new RuntimeException("Không thể cập nhật phần thưởng với xác suất " + newProbability + "%. " +
                                     "Tổng xác suất các phần thưởng đang hoạt động: " + totalActiveProbability + "%, " +
                                     "Chỉ còn lại: " + remaining + "%. Quy tắc: chỉ tính phần thưởng active (status=1)");
        }
    }
    
    /**
     *  NEW: Tính tổng xác suất của các phần thưởng ACTIVE (status=1)
     */
    private BigDecimal calculateTotalActiveProbability(Integer campaignId, Integer excludeRewardId) {
        List<Reward> rewards = rewardRepository.findByCampaignIdOrderByProbabilityDesc(campaignId);
        return rewards.stream()
            .filter(r -> !r.getId().equals(excludeRewardId)) // Loại bỏ reward hiện tại (nếu có)
            .filter(r -> r.getStatus() != null && r.getStatus() == 1) // ✅ CHỈ TÍNH CÁC PHẦN THƯỞNG ACTIVE
            .map(Reward::getProbability)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     *  NEW: Validate khi bật phần thưởng để đảm bảo tổng <= 100%
     */
    private void validateActiveProbabilityWhenEnabling(Integer campaignId, BigDecimal rewardProbability) {
        BigDecimal totalActiveProbability = calculateTotalActiveProbability(campaignId, null);
        BigDecimal newTotal = totalActiveProbability.add(rewardProbability);
        if (newTotal.compareTo(new BigDecimal("100")) > 0) {
            BigDecimal remaining = new BigDecimal("100").subtract(totalActiveProbability);
            throw new RuntimeException("Không thể bật phần thưởng với xác suất " + rewardProbability + "%. " +
                                     "Tổng xác suất các phần thưởng đang hoạt động: " + totalActiveProbability + "%, " +
                                     "Chỉ còn lại: " + remaining + "%. Quy tắc: tổng các phần thưởng active <= 100%");
        }
    }
    
    /**
     * NEW: API riêng cho thông tin xác suất chiến dịch
     */
    @Override
    public CampaignProbabilityResponse getCampaignProbabilityInfo(Integer campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));
        
        List<Reward> allRewards = rewardRepository.findByCampaignIdOrderByProbabilityDesc(campaignId);
        List<Reward> activeRewards = allRewards.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .collect(Collectors.toList());
        
        BigDecimal totalActiveProbability = activeRewards.stream()
                .map(Reward::getProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal remainingProbability = new BigDecimal("100").subtract(totalActiveProbability);
        
        CampaignProbabilityResponse response = new CampaignProbabilityResponse();
        response.setCampaignId(campaignId);
        response.setCampaignName(campaign.getName());
        response.setTotalActiveProbability(totalActiveProbability);
        response.setRemainingProbability(remainingProbability);
        response.setActiveRewardsCount(activeRewards.size());
        response.setTotalRewardsCount(allRewards.size());
        
        return response;
    }
}
