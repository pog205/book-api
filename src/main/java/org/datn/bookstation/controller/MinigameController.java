package org.datn.bookstation.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.minigame.OpenBoxRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.minigame.OpenBoxResponse;
import org.datn.bookstation.dto.response.minigame.BoxHistoryResponse;
import org.datn.bookstation.dto.response.minigame.UserCampaignStatsResponse;
import org.datn.bookstation.service.MinigameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/minigame")
@AllArgsConstructor
@Slf4j
public class MinigameController {

    private final MinigameService minigameService;

    /**
     * 🎮 MỞ HỘP - API chính của minigame
     * Logic: User chọn chiến dịch → Backend random → Trả kết quả → Lưu lịch sử → Update phần thưởng
     */
    @PostMapping("/open-box")
    public ResponseEntity<ApiResponse<OpenBoxResponse>> openBox(@Valid @RequestBody OpenBoxRequest request) {
        try {
            log.info("🎮 User {} opening box in campaign {} with type {}", 
                     request.getUserId(), request.getCampaignId(), request.getOpenType());
            
            OpenBoxResponse result = minigameService.openBox(request);
            
            ApiResponse<OpenBoxResponse> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    result.isSuccess() ? "Mở hộp thành công!" : "Mở hộp thất bại!",
                    result
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error opening box: ", e);
            
            OpenBoxResponse failedResult = new OpenBoxResponse();
            failedResult.setSuccess(false);
            failedResult.setMessage("Lỗi hệ thống: " + e.getMessage());
            failedResult.setHasReward(false);
            failedResult.setAnimationType("error");
            
            ApiResponse<OpenBoxResponse> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi mở hộp: " + e.getMessage(),
                    failedResult
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy lịch sử mở hộp của user
     */
    @GetMapping("/history/user/{userId}")
    public ResponseEntity<ApiResponse<List<BoxHistoryResponse>>> getUserHistory(
            @PathVariable Integer userId,
            @RequestParam(required = false) Integer campaignId) {
        try {
            List<BoxHistoryResponse> history = minigameService.getUserHistory(userId, campaignId);
            ApiResponse<List<BoxHistoryResponse>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy lịch sử mở hộp thành công",
                    history
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting user history: ", e);
            ApiResponse<List<BoxHistoryResponse>> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi lấy lịch sử: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thống kê user trong chiến dịch
     */
    @GetMapping("/stats/user/{userId}/campaign/{campaignId}")
    public ResponseEntity<ApiResponse<UserCampaignStatsResponse>> getUserCampaignStats(
            @PathVariable Integer userId,
            @PathVariable Integer campaignId) {
        try {
            UserCampaignStatsResponse stats = minigameService.getUserCampaignStats(userId, campaignId);
            ApiResponse<UserCampaignStatsResponse> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thống kê thành công",
                    stats
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting user campaign stats: ", e);
            ApiResponse<UserCampaignStatsResponse> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi lấy thống kê: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy lịch sử mở hộp của chiến dịch (Admin)
     */
    @GetMapping("/history/campaign/{campaignId}")
    public ResponseEntity<ApiResponse<List<BoxHistoryResponse>>> getCampaignHistory(@PathVariable Integer campaignId) {
        try {
            List<BoxHistoryResponse> history = minigameService.getCampaignHistory(campaignId);
            ApiResponse<List<BoxHistoryResponse>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy lịch sử chiến dịch thành công",
                    history
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting campaign history: ", e);
            ApiResponse<List<BoxHistoryResponse>> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi lấy lịch sử chiến dịch: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
