package org.datn.bookstation.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.minigame.RewardRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.minigame.RewardResponse;
import org.datn.bookstation.dto.response.minigame.CampaignProbabilityResponse;
import org.datn.bookstation.service.RewardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@AllArgsConstructor
@Slf4j
public class RewardController {

    private final RewardService rewardService;

    /**
     * Lấy danh sách phần thưởng của chiến dịch
     */
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<ApiResponse<List<RewardResponse>>> getRewardsByCampaign(@PathVariable Integer campaignId) {
        try {
            List<RewardResponse> rewards = rewardService.getRewardsByCampaign(campaignId);
            ApiResponse<List<RewardResponse>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách phần thưởng thành công",
                    rewards
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting rewards by campaign: ", e);
            ApiResponse<List<RewardResponse>> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi lấy danh sách phần thưởng: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * ✅ NEW: Lấy thông tin xác suất của chiến dịch (API riêng)
     */
    @GetMapping("/campaign/{campaignId}/probability-info")
    public ResponseEntity<ApiResponse<CampaignProbabilityResponse>> getCampaignProbabilityInfo(@PathVariable Integer campaignId) {
        try {
            CampaignProbabilityResponse probabilityInfo = rewardService.getCampaignProbabilityInfo(campaignId);
            ApiResponse<CampaignProbabilityResponse> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin xác suất chiến dịch thành công",
                    probabilityInfo
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting campaign probability info: ", e);
            ApiResponse<CampaignProbabilityResponse> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy thông tin phần thưởng theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RewardResponse>> getRewardById(@PathVariable Integer id) {
        try {
            RewardResponse reward = rewardService.getRewardById(id);
            ApiResponse<RewardResponse> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin phần thưởng thành công",
                    reward
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting reward by id: ", e);
            ApiResponse<RewardResponse> response = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Tạo phần thưởng mới (Admin)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createReward(@Valid @RequestBody RewardRequest request) {
        try {
            rewardService.createReward(request);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.CREATED.value(),
                    "Tạo phần thưởng thành công",
                    "Reward created successfully"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating reward: ", e);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi tạo phần thưởng: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật phần thưởng (Admin)
     */
    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateReward(@Valid @RequestBody RewardRequest request) {
        try {
            if (request.getId() == null) {
                throw new RuntimeException("ID phần thưởng không được để trống");
            }
            rewardService.updateReward(request);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Cập nhật phần thưởng thành công",
                    "Reward updated successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating reward: ", e);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi cập nhật phần thưởng: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật trạng thái phần thưởng (Admin)
     */
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(@RequestParam Integer id) {
        try {
            rewardService.toggleStatus(id, 0);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Đã chuyển trạng thái phần thưởng thành công",
                    "Reward status toggled successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling reward status: ", e);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi chuyển trạng thái phần thưởng: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa phần thưởng (Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteReward(@PathVariable Integer id) {
        try {
            rewardService.deleteReward(id);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Xóa phần thưởng thành công",
                    "Reward deleted successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting reward: ", e);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi xóa phần thưởng: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}
