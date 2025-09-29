package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickActionResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionResult {
        private String flashSaleId;
        private String flashSaleName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String expectedImpact;
        private String trackingUrl;
        private Integer vouchersSent;
        private Integer ordersCreated;
        private String campaignId;
        private String callsInitiated;
        private String emailsSent;
    }

    private String actionId;
    private String actionType;
    private String status; // "completed", "processing", "failed"
    private ActionResult result;
    private String errorMessage;
    private LocalDateTime executedAt;
    private Map<String, Object> metadata;
}
