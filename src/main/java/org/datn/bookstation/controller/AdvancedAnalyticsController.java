package org.datn.bookstation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.datn.bookstation.dto.request.QuickActionRequest;
import org.datn.bookstation.dto.response.*;
import org.datn.bookstation.service.AdvancedAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Advanced Business Intelligence Controller
 * Provides AI-powered dashboard insights and automated business actions
 * All endpoints require ADMIN role access
 */
@Slf4j
@RestController
@RequestMapping("/api/advanced-analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdvancedAnalyticsController {

    private final AdvancedAnalyticsService advancedAnalyticsService;

    /**
     * 📊 Get Survival KPIs - Critical business health metrics
     * GET /api/advanced-analytics/survival-kpis
     */
    @GetMapping("/survival-kpis")
    public ResponseEntity<ApiResponse<SurvivalKpiResponse>> getSurvivalKpis() {
        log.info("📊 [API] Getting survival KPIs");
        
        try {
            SurvivalKpiResponse response = advancedAnalyticsService.getSurvivalKpis();
            
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy KPI sống còn thành công", response));
                    
        } catch (Exception e) {
            log.error("❌ [API] Error getting survival KPIs: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<SurvivalKpiResponse>(500, "Lỗi server khi lấy KPI sống còn: " + e.getMessage(), null));
        }
    }

    /**
     * 🎯 Get Opportunity Radar - Business opportunity detection
     * GET /api/advanced-analytics/opportunity-radar
     */
    @GetMapping("/opportunity-radar")
    public ResponseEntity<ApiResponse<OpportunityRadarResponse>> getOpportunityRadar() {
        log.info("🎯 [API] Getting opportunity radar");
        
        try {
            OpportunityRadarResponse response = advancedAnalyticsService.getOpportunityRadar();
            
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy radar cơ hội thành công", response));
                    
        } catch (Exception e) {
            log.error("❌ [API] Error getting opportunity radar: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<OpportunityRadarResponse>(500, "Lỗi server khi lấy radar cơ hội: " + e.getMessage(), null));
        }
    }

    /**
     * 🗺️ Get Order Health Map - Regional and shipping health analysis
     * GET /api/advanced-analytics/order-health-map
     */
    @GetMapping("/order-health-map")
    public ResponseEntity<ApiResponse<OrderHealthMapResponse>> getOrderHealthMap() {
        log.info("🗺️ [API] Getting order health map");
        
        try {
            OrderHealthMapResponse response = advancedAnalyticsService.getOrderHealthMap();
            
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy bản đồ sức khỏe đơn hàng thành công", response));
                    
        } catch (Exception e) {
            log.error("❌ [API] Error getting order health map: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<OrderHealthMapResponse>(500, "Lỗi server khi lấy bản đồ sức khỏe: " + e.getMessage(), null));
        }
    }

    /**
     * 🚨 Get Real-time Alerts - Critical system alerts and monitoring
     * GET /api/advanced-analytics/real-time-alerts
     */
    @GetMapping("/real-time-alerts")
    public ResponseEntity<ApiResponse<RealTimeAlertsResponse>> getRealTimeAlerts() {
        log.info("🚨 [API] Getting real-time alerts");
        
        try {
            RealTimeAlertsResponse response = advancedAnalyticsService.getRealTimeAlerts();
            
            return ResponseEntity.ok(new ApiResponse<>(200, "Lấy cảnh báo thời gian thực thành công", response));
                    
        } catch (Exception e) {
            log.error("❌ [API] Error getting real-time alerts: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<RealTimeAlertsResponse>(500, "Lỗi server khi lấy cảnh báo: " + e.getMessage(), null));
        }
    }

    /**
     * ⚡ Execute Quick Action - Automated business actions
     * POST /api/advanced-analytics/quick-actions
     * 
     * @param request Action type and parameters
     */
    @PostMapping("/quick-actions")
    public ResponseEntity<ApiResponse<QuickActionResponse>> executeQuickAction(
            @RequestBody QuickActionRequest request) {
        log.info("⚡ [API] Executing quick action: {}", request.getActionType());
        
        try {
            // Validate request
            if (request.getActionType() == null || request.getActionType().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<QuickActionResponse>(400, "Action type không được để trống", null));
            }

            QuickActionResponse response = advancedAnalyticsService.executeQuickAction(request);
            
            if ("failed".equals(response.getStatus())) {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponse<QuickActionResponse>(500, "Thực hiện hành động thất bại: " + response.getErrorMessage(), response));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(200, "Hành động được thực hiện thành công", response));
                    
        } catch (IllegalArgumentException e) {
            log.error("❌ [API] Invalid quick action request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<QuickActionResponse>(400, "Loại hành động không hợp lệ: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("❌ [API] Error executing quick action: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<QuickActionResponse>(500, "Lỗi server khi thực hiện hành động: " + e.getMessage(), null));
        }
    }
}
