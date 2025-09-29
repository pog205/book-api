package org.datn.bookstation.service.impl.minigame;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.minigame.OpenBoxRequest;
import org.datn.bookstation.dto.response.minigame.OpenBoxResponse;
import org.datn.bookstation.dto.response.minigame.BoxHistoryResponse;
import org.datn.bookstation.dto.response.minigame.UserCampaignStatsResponse;
import org.datn.bookstation.entity.*;
import org.datn.bookstation.entity.enums.BoxOpenType;
import org.datn.bookstation.entity.enums.RewardType;
import org.datn.bookstation.repository.*;
import org.datn.bookstation.service.MinigameService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class MinigameServiceImpl implements MinigameService {

    private final CampaignRepository campaignRepository;
    private final RewardRepository rewardRepository;
    private final UserCampaignRepository userCampaignRepository;
    private final BoxHistoryRepository boxHistoryRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final UserVoucherRepository userVoucherRepository;
    
    private final Random random = new Random();

    @Override
    public OpenBoxResponse openBox(OpenBoxRequest request) {
        log.info("User {} attempting to open box in campaign {} with type {}", 
                 request.getUserId(), request.getCampaignId(), request.getOpenType());

        // 0. VALIDATION - Kiểm tra dữ liệu frontend có khớp với backend không
        List<String> validationErrors = validateFrontendData(request);
        if (!validationErrors.isEmpty()) {
            log.warn("Validation failed for user {}: {}", request.getUserId(), validationErrors);
            return new OpenBoxResponse(false, "Dữ liệu không đồng bộ, vui lòng tải lại trang!", 
                                     validationErrors, true);
        }

        // 1. Validate campaign
        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new RuntimeException("Chiến dịch không tồn tại"));
        
        long currentTime = System.currentTimeMillis();
        if (campaign.getStatus() != 1 || currentTime < campaign.getStartDate() || currentTime > campaign.getEndDate()) {
            throw new RuntimeException("Chiến dịch không hoạt động hoặc đã hết hạn");
        }

        // 2. Validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // 3. Get or create UserCampaign
        UserCampaign userCampaign = userCampaignRepository.findByUserIdAndCampaignId(request.getUserId(), request.getCampaignId())
                .orElseGet(() -> {
                    UserCampaign newUserCampaign = new UserCampaign();
                    newUserCampaign.setUser(user);
                    newUserCampaign.setCampaign(campaign);
                    newUserCampaign.setFreeOpenedCount(0);
                    newUserCampaign.setTotalOpenedCount(0);
                    return userCampaignRepository.save(newUserCampaign);
                });

        // 4. Validate open type and user eligibility
        if (request.getOpenType() == BoxOpenType.FREE) {
            if (userCampaign.getFreeOpenedCount() >= campaign.getConfigFreeLimit()) {
                return new OpenBoxResponse(false, "Bạn đã hết lượt mở miễn phí", BoxOpenType.FREE, 
                                         0, user.getTotalPoint());
            }
        } else if (request.getOpenType() == BoxOpenType.POINT) {
            if (user.getTotalPoint() < campaign.getConfigPointCost()) {
                return new OpenBoxResponse(false, "Bạn không đủ điểm để mở hộp", BoxOpenType.POINT,
                                         campaign.getConfigFreeLimit() - userCampaign.getFreeOpenedCount(),
                                         user.getTotalPoint());
            }
        }

        // 5. Get available rewards
        List<Reward> availableRewards = rewardRepository.findAvailableByCampaignId(request.getCampaignId());
        if (availableRewards.isEmpty()) {
            throw new RuntimeException("Chiến dịch không có phần thưởng nào khả dụng");
        }

        // 6. Calculate win probability boost based on user open count (càng mở nhiều, tỷ lệ trúng càng cao)
        double winProbabilityBoost = calculateWinProbabilityBoost(userCampaign.getTotalOpenedCount());
        log.info("User {} has opened {} times, win probability boost: {}%", 
                 user.getId(), userCampaign.getTotalOpenedCount(), winProbabilityBoost * 100);

        // 7. Random reward selection
        Reward selectedReward = selectReward(availableRewards, winProbabilityBoost);

        // 8. Process the box opening
        return processBoxOpening(user, campaign, userCampaign, selectedReward, request.getOpenType());
    }

    @Override
    public List<BoxHistoryResponse> getUserHistory(Integer userId, Integer campaignId) {
        List<BoxHistory> histories;
        if (campaignId != null) {
            histories = boxHistoryRepository.findByUserIdAndCampaignIdOrderByOpenDateDesc(userId, campaignId);
        } else {
            histories = boxHistoryRepository.findByUserIdOrderByOpenDateDesc(userId);
        }
        
        return histories.stream()
                .map(this::toBoxHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserCampaignStatsResponse getUserCampaignStats(Integer userId, Integer campaignId) {
        UserCampaign userCampaign = userCampaignRepository.findByUserIdAndCampaignId(userId, campaignId)
                .orElse(null);
        
        if (userCampaign == null) {
            // Return default stats if user hasn't participated
            return new UserCampaignStatsResponse(
                userId, null, campaignId, null, 
                0, 0, 0,
                0L, 0L, 0.0,
                0, 0, 0,
                System.currentTimeMillis(), System.currentTimeMillis()
            );
        }

        Campaign campaign = userCampaign.getCampaign();
        User user = userCampaign.getUser();
        
        // Calculate win/lose stats
        Long totalWins = boxHistoryRepository.countWinsByUserAndCampaign(userId, campaignId);
        Long totalLoses = boxHistoryRepository.countByUserIdAndCampaignId(userId, campaignId) - totalWins;
        Double winRate = userCampaign.getTotalOpenedCount() > 0 
            ? (totalWins.doubleValue() / userCampaign.getTotalOpenedCount()) * 100 
            : 0.0;

        // Calculate reward stats (simplified - would need more complex queries for exact numbers)
        int remainingFreeOpens = Math.max(0, campaign.getConfigFreeLimit() - userCampaign.getFreeOpenedCount());

        return new UserCampaignStatsResponse(
            userId, user.getFullName(), campaignId, campaign.getName(),
            userCampaign.getFreeOpenedCount(), userCampaign.getTotalOpenedCount(), remainingFreeOpens,
            totalWins, totalLoses, winRate,
            0, 0, 0, // TODO: Add voucher/point statistics if needed
            userCampaign.getCreatedAt(), userCampaign.getUpdatedAt()
        );
    }

    @Override
    public List<BoxHistoryResponse> getCampaignHistory(Integer campaignId) {
        List<BoxHistory> histories = boxHistoryRepository.findByCampaignIdOrderByOpenDateDesc(campaignId);
        return histories.stream()
                .map(this::toBoxHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tính toán độ tăng tỷ lệ trúng dựa trên số lần mở
     * Người dùng mở càng nhiều thì tỷ lệ trúng các phần thưởng quý càng cao
     */
    private double calculateWinProbabilityBoost(Integer totalOpenedCount) {
        if (totalOpenedCount == null) totalOpenedCount = 0;
        
        // Base boost formula: 0.5% per open, max 20% boost at 40 opens
        double boost = Math.min(totalOpenedCount * 0.005, 0.20);
        return boost;
    }

    /**
     * Chọn phần thưởng dựa trên xác suất và boost
     */
    private Reward selectReward(List<Reward> availableRewards, double winProbabilityBoost) {
        // Sort rewards: NONE first, then POINTS, then VOUCHER (valuable rewards last)
        availableRewards.sort((r1, r2) -> {
            int typeOrder1 = getRewardTypeOrder(r1.getType());
            int typeOrder2 = getRewardTypeOrder(r2.getType());
            return Integer.compare(typeOrder1, typeOrder2);
        });

        double totalProbability = 0;
        for (Reward reward : availableRewards) {
            double adjustedProbability = reward.getProbability().doubleValue();
            
            // Apply boost only to valuable rewards (POINTS, VOUCHER)
            if (reward.getType() != RewardType.NONE) {
                adjustedProbability += winProbabilityBoost * 100; // Convert to percentage
                adjustedProbability = Math.min(adjustedProbability, 100.0); // Cap at 100%
            }
            
            totalProbability += adjustedProbability;
        }

        // Random selection
        double randomValue = random.nextDouble() * totalProbability;
        double cumulativeProbability = 0;

        for (Reward reward : availableRewards) {
            double adjustedProbability = reward.getProbability().doubleValue();
            if (reward.getType() != RewardType.NONE) {
                adjustedProbability += winProbabilityBoost * 100;
                adjustedProbability = Math.min(adjustedProbability, 100.0);
            }
            
            cumulativeProbability += adjustedProbability;
            
            if (randomValue <= cumulativeProbability) {
                log.info("Selected reward: {} (type: {}, probability: {}%, adjusted: {}%)", 
                         reward.getName(), reward.getType(), 
                         reward.getProbability(), adjustedProbability);
                return reward;
            }
        }

        // Fallback to first reward (should not happen)
        return availableRewards.get(0);
    }

    private int getRewardTypeOrder(RewardType type) {
        switch (type) {
            case NONE: return 1;
            case POINTS: return 2;
            case VOUCHER: return 3;
            default: return 1;
        }
    }

    /**
     * Xử lý mở hộp và cập nhật dữ liệu
     */
    private OpenBoxResponse processBoxOpening(User user, Campaign campaign, UserCampaign userCampaign, 
                                            Reward selectedReward, BoxOpenType openType) {
        
        // 1. Update user campaign stats
        userCampaign.setTotalOpenedCount(userCampaign.getTotalOpenedCount() + 1);
        Integer pointsSpent = 0;
        
        if (openType == BoxOpenType.FREE) {
            userCampaign.setFreeOpenedCount(userCampaign.getFreeOpenedCount() + 1);
        } else if (openType == BoxOpenType.POINT) {
            pointsSpent = campaign.getConfigPointCost();
            user.setTotalPoint(user.getTotalPoint() - pointsSpent);
            userRepository.save(user);
            
            // 🔄 KHÔNG tạo bản ghi Point riêng cho việc tiêu điểm ở đây
            // Sẽ được gộp chung với phần thưởng điểm (nếu có) trong processReward()
            log.info("User {} spent {} points to open box in campaign {}", user.getId(), pointsSpent, campaign.getName());
        }
        
        userCampaignRepository.save(userCampaign);

        // 2. Process reward if not NONE
        if (selectedReward.getType() != RewardType.NONE && selectedReward.getStock() > 0) {
            processReward(user, selectedReward, campaign.getName(), pointsSpent);
            
            // Update reward stock
            selectedReward.setStock(selectedReward.getStock() - 1);
            rewardRepository.save(selectedReward);
        } else if (pointsSpent > 0) {
            //  FIX: Trường hợp không trúng gì (NONE) nhưng đã chi điểm
            Point spentOnlyPoint = new Point();
            spentOnlyPoint.setUser(user);
            spentOnlyPoint.setPointSpent(pointsSpent);
            spentOnlyPoint.setDescription("Mở hộp chiến dịch: " + campaign.getName() + " (chi " + pointsSpent + " điểm, không trúng thưởng)");
            spentOnlyPoint.setCreatedAt(System.currentTimeMillis());
            spentOnlyPoint.setStatus((byte) 1);
            pointRepository.save(spentOnlyPoint);
            log.info("Created point record for {} points spent with no reward", pointsSpent);
        }

        // 3. Save box history
        BoxHistory history = new BoxHistory();
        history.setUser(user);
        history.setCampaign(campaign);
        history.setOpenType(openType);
        history.setPointsSpent(pointsSpent);
        
        if (selectedReward.getType() != RewardType.NONE && selectedReward.getStock() >= 0) {
            history.setReward(selectedReward);
            history.setRewardValue(getRewardValue(selectedReward));
        }
        
        boxHistoryRepository.save(history);

        // 4. Build response
        OpenBoxResponse response = new OpenBoxResponse();
        response.setSuccess(true);
        response.setHistoryId(history.getId());
        response.setOpenType(openType);
        response.setOpenDate(history.getOpenDate());
        response.setPointsSpent(pointsSpent);
        
        // User info after opening
        response.setUserRemainingFreeOpens(campaign.getConfigFreeLimit() - userCampaign.getFreeOpenedCount());
        response.setUserCurrentPoints(user.getTotalPoint());
        response.setUserTotalOpenedInCampaign(userCampaign.getTotalOpenedCount());

        // Reward info
        if (selectedReward.getType() != RewardType.NONE) {
            response.setHasReward(true);
            response.setRewardType(selectedReward.getType());
            response.setRewardName(selectedReward.getName());
            response.setRewardDescription(selectedReward.getDescription());
            response.setRewardValue(getRewardValue(selectedReward));
            response.setAnimationType(selectedReward.getType() == RewardType.VOUCHER ? "big_win" : "win");
            
            if (selectedReward.getVoucher() != null) {
                response.setVoucherId(selectedReward.getVoucher().getId());
                response.setVoucherCode(selectedReward.getVoucher().getCode());
                response.setVoucherName(selectedReward.getVoucher().getName());
            }
            
            response.setMessage("Chúc mừng! Bạn đã trúng " + selectedReward.getName());
        } else {
            response.setHasReward(false);
            response.setAnimationType("lose");
            response.setMessage("Chúc bạn may mắn lần sau!");
        }

        log.info("Box opening completed for user {} in campaign {}: {} - {}", 
                 user.getId(), campaign.getId(), selectedReward.getType(), selectedReward.getName());

        return response;
    }

    private void processReward(User user, Reward reward, String campaignName, Integer pointsSpent) {
        switch (reward.getType()) {
            case POINTS:
                // Add points to user
                user.setTotalPoint(user.getTotalPoint() + reward.getPointValue());
                userRepository.save(user);
                
                //  FIX: Tạo 1 bản ghi Point duy nhất với cả pointSpent và pointEarned
                Point combinedPoint = new Point();
                combinedPoint.setUser(user);
                combinedPoint.setPointEarned(reward.getPointValue());
                
                if (pointsSpent > 0) {
                    //  GỘP: Bao gồm cả điểm chi và điểm nhận trong 1 record
                    combinedPoint.setPointSpent(pointsSpent);
                    combinedPoint.setDescription("Trúng thưởng " + reward.getPointValue() + " điểm từ chiến dịch " + campaignName + 
                                               " (đã chi " + pointsSpent + " điểm để mở hộp)");
                } else {
                    combinedPoint.setDescription("Trúng thưởng " + reward.getPointValue() + " điểm từ chiến dịch " + campaignName + 
                                               " (mở miễn phí)");
                }
                combinedPoint.setCreatedAt(System.currentTimeMillis());
                combinedPoint.setStatus((byte) 1);
                pointRepository.save(combinedPoint);
                
                log.info(" FIXED: Combined point record - Awarded {} points to user {} (spent {} points to open box)", 
                         reward.getPointValue(), user.getId(), pointsSpent);
                break;
                
            case VOUCHER:
                //  Tạo bản ghi riêng cho việc tiêu điểm (nếu có) khi nhận voucher
                if (pointsSpent > 0) {
                    Point spentPoint = new Point();
                    spentPoint.setUser(user);
                    spentPoint.setPointSpent(pointsSpent);
                    spentPoint.setDescription("Mở hộp chiến dịch: " + campaignName + " (chi " + pointsSpent + " điểm)");
                    spentPoint.setCreatedAt(System.currentTimeMillis());
                    spentPoint.setStatus((byte) 1);
                    pointRepository.save(spentPoint);
                    log.info("Created separate point record for spending {} points to get voucher", pointsSpent);
                }
                
                if (reward.getVoucher() != null) {
                    //  FIXED: Always create new UserVoucher record, no limit checking
                    // User requirement: "không có ngăn ngừa gì hết phải tạo thêm bản ghi"
                    UserVoucher userVoucher = new UserVoucher();
                    userVoucher.setUser(user);
                    userVoucher.setVoucher(reward.getVoucher());
                    userVoucherRepository.save(userVoucher);
                    log.info(" FIXED: Always awarded voucher {} to user {} (new record created)", 
                             reward.getVoucher().getCode(), user.getId());
                }
                break;
                
            case NONE:
            default:
                // No reward processing needed
                break;
        }
    }

    private Integer getRewardValue(Reward reward) {
        switch (reward.getType()) {
            case POINTS:
                return reward.getPointValue();
            case VOUCHER:
                return reward.getVoucher() != null ? reward.getVoucher().getId() : null;
            default:
                return null;
        }
    }

    private BoxHistoryResponse toBoxHistoryResponse(BoxHistory history) {
        BoxHistoryResponse response = new BoxHistoryResponse();
        
        response.setId(history.getId());
        response.setUserId(history.getUser().getId());
        response.setUserName(history.getUser().getFullName());
        response.setCampaignId(history.getCampaign().getId());
        response.setCampaignName(history.getCampaign().getName());
        response.setOpenType(history.getOpenType());
        response.setOpenDate(history.getOpenDate());
        response.setPointsSpent(history.getPointsSpent());
        response.setCreatedAt(history.getCreatedAt());
        
        if (history.getReward() != null) {
            response.setWin(true);
            response.setRewardId(history.getReward().getId());
            response.setRewardType(history.getReward().getType());
            response.setRewardName(history.getReward().getName());
            response.setRewardValue(history.getRewardValue());
            response.setDisplayResult("Trúng: " + history.getReward().getName());
            
            if (history.getReward().getVoucher() != null) {
                response.setVoucherId(history.getReward().getVoucher().getId());
                response.setVoucherCode(history.getReward().getVoucher().getCode());
                response.setVoucherName(history.getReward().getVoucher().getName());
            }
        } else {
            response.setWin(false);
            response.setDisplayResult("Chúc bạn may mắn lần sau");
        }
        
        return response;
    }

    /**
     *  REMOVED: No longer checking voucher limits per user request
     * User requirement: "không có ngăn ngừa gì hết phải tạo thêm bản ghi"
     */
    /*
    private boolean canUserReceiveVoucher(Integer userId, Voucher voucher) {
        if (voucher.getUsageLimitPerUser() == null) {
            return true; // No user limit set
        }
        
        //  UPDATED: Đếm số records UserVoucher của user cho voucher này
        // Mỗi record = 1 lần nhận voucher (không dùng quantity nữa)
        List<UserVoucher> userVouchers = userVoucherRepository.findAll().stream()
                .filter(uv -> uv.getUser().getId().equals(userId) && 
                             uv.getVoucher().getId().equals(voucher.getId()))
                .toList();
        
        return userVouchers.size() < voucher.getUsageLimitPerUser();
    }
    */
    
    /**
     *  VALIDATION - Kiểm tra dữ liệu frontend có khớp với backend không
     * Mục đích: Tránh trường hợp user đang xem giao diện cũ nhưng admin đã thay đổi config
     */
    private List<String> validateFrontendData(OpenBoxRequest request) {
        List<String> errors = new java.util.ArrayList<>();
        
        try {
            // 1. Validate campaign config
            Campaign campaign = campaignRepository.findById(request.getCampaignId()).orElse(null);
            if (campaign == null) {
                errors.add("Chiến dịch không tồn tại");
                return errors;
            }
            
            // 2. Validate free limit
            if (!request.getFrontendFreeLimit().equals(campaign.getConfigFreeLimit())) {
                errors.add(String.format("Số lượt mở miễn phí đã thay đổi: %d → %d", 
                          request.getFrontendFreeLimit(), campaign.getConfigFreeLimit()));
            }
            
            // 3. Validate point cost
            if (!request.getFrontendPointCost().equals(campaign.getConfigPointCost())) {
                errors.add(String.format("Điểm cần để mở hộp đã thay đổi: %d → %d", 
                          request.getFrontendPointCost(), campaign.getConfigPointCost()));
            }
            
            // 4. Validate campaign time
            if (!request.getFrontendStartDate().equals(campaign.getStartDate()) ||
                !request.getFrontendEndDate().equals(campaign.getEndDate())) {
                errors.add("Thời gian chiến dịch đã được cập nhật");
            }
            
            // 5. Validate user point
            User user = userRepository.findById(request.getUserId()).orElse(null);
            if (user != null && !request.getFrontendUserPoint().equals(user.getTotalPoint())) {
                errors.add(String.format("Điểm của bạn đã thay đổi: %d → %d", 
                          request.getFrontendUserPoint(), user.getTotalPoint()));
            }
            
            // 6. Validate user free opened count  
            UserCampaign userCampaign = userCampaignRepository.findByUserIdAndCampaignId(request.getUserId(), request.getCampaignId()).orElse(null);
            if (userCampaign != null && !request.getFrontendFreeOpenedCount().equals(userCampaign.getFreeOpenedCount())) {
                errors.add(String.format("Số lần đã mở miễn phí đã thay đổi: %d → %d", 
                          request.getFrontendFreeOpenedCount(), userCampaign.getFreeOpenedCount()));
            }
            
        } catch (Exception e) {
            log.error("Error validating frontend data: ", e);
            errors.add("Lỗi khi kiểm tra dữ liệu: " + e.getMessage());
        }
        
        return errors;
    }
}
