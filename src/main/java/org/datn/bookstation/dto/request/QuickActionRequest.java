package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickActionRequest {
    
    private String actionType; // "CREATE_FLASH_SALE", "SEND_VIP_VOUCHER", "BULK_INVENTORY_ORDER", etc.
    private Map<String, Object> parameters;
    
    // Common parameter examples:
    // For CREATE_FLASH_SALE: bookIds, discountPercentage, duration, targetRegion
    // For SEND_VIP_VOUCHER: userIds, voucherType, discountValue
    // For BULK_INVENTORY_ORDER: bookIds, quantities
    // For AUTO_COD_CALL: orderIds, callType
    // For RECOVERY_EMAIL_CAMPAIGN: userIds, emailTemplate, discountOffer
    // For REGIONAL_MARKETING_BOOST: region, campaignType, budget
}
