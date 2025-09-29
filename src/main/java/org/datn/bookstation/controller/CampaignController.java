package org.datn.bookstation.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.minigame.CampaignRequest;
import org.datn.bookstation.dto.response.ApiResponse;
import org.datn.bookstation.dto.response.PaginationResponse;
import org.datn.bookstation.dto.response.minigame.CampaignResponse;
import org.datn.bookstation.service.CampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@AllArgsConstructor
@Slf4j
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * Lấy danh sách chiến dịch với phân trang (Admin)
     */
    @GetMapping
    public ResponseEntity<PaginationResponse<CampaignResponse>> getAllCampaigns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Byte status) {
        
        PaginationResponse<CampaignResponse> response = campaignService.getAllWithPagination(page, size, name, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách chiến dịch đang hoạt động (Client)
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getActiveCampaigns() {
        try {
            List<CampaignResponse> campaigns = campaignService.getActiveCampaigns();
            ApiResponse<List<CampaignResponse>> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy danh sách chiến dịch hoạt động thành công",
                    campaigns
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting active campaigns: ", e);
            ApiResponse<List<CampaignResponse>> response = new ApiResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Lỗi khi lấy danh sách chiến dịch: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin chi tiết chiến dịch
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaignById(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId) {
        try {
            CampaignResponse campaign = campaignService.getCampaignById(id, userId);
            ApiResponse<CampaignResponse> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin chiến dịch thành công",
                    campaign
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting campaign by id: ", e);
            ApiResponse<CampaignResponse> response = new ApiResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Tạo chiến dịch mới (Admin)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createCampaign(@Valid @RequestBody CampaignRequest request) {
        try {
            campaignService.createCampaign(request);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.CREATED.value(),
                    "Tạo chiến dịch thành công",
                    "Campaign created successfully"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating campaign: ", e);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi tạo chiến dịch: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật chiến dịch (Admin)
     */
    @PutMapping
    public ResponseEntity<ApiResponse<String>> updateCampaign(@Valid @RequestBody CampaignRequest request) {
        try {
            if (request.getId() == null) {
                throw new RuntimeException("ID chiến dịch không được để trống");
            }
            campaignService.updateCampaign(request);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Cập nhật chiến dịch thành công",
                    "Campaign updated successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating campaign: ", e);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi cập nhật chiến dịch: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật trạng thái chiến dịch (Admin)
     */
    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(@RequestParam Integer id) {
        try {
            campaignService.toggleStatus(id, 0);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Đã chuyển trạng thái chiến dịch thành công",
                    "Campaign status toggled successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error toggling campaign status: ", e);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi chuyển trạng thái chiến dịch: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa chiến dịch (Admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCampaign(@PathVariable Integer id) {
        try {
            campaignService.deleteCampaign(id);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "Xóa chiến dịch thành công",
                    "Campaign deleted successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting campaign: ", e);
            ApiResponse<String> response = new ApiResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Lỗi khi xóa chiến dịch: " + e.getMessage(),
                    null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}
