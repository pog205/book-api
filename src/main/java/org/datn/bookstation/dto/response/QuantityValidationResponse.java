package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuantityValidationResponse {
    
    private Boolean valid;
    private String message;
    private Integer availableQuantity;
    private Boolean isFlashSale; // Có phải flash sale không
    private Integer flashSaleStock; // Số lượng flash sale còn lại
    private Integer maxPurchasePerUser; // Giới hạn mua per user
    
    public static QuantityValidationResponse success(Integer availableQuantity) {
        return new QuantityValidationResponse(true, "Số lượng hợp lệ", availableQuantity, false, null, null);
    }
    
    public static QuantityValidationResponse failure(String message, Integer availableQuantity) {
        return new QuantityValidationResponse(false, message, availableQuantity, false, null, null);
    }
    
    public static QuantityValidationResponse flashSaleSuccess(Integer availableQuantity, Integer flashSaleStock, Integer maxPurchasePerUser) {
        return new QuantityValidationResponse(true, "Số lượng flash sale hợp lệ", availableQuantity, true, flashSaleStock, maxPurchasePerUser);
    }
    
    public static QuantityValidationResponse flashSaleFailure(String message, Integer availableQuantity, Integer flashSaleStock, Integer maxPurchasePerUser) {
        return new QuantityValidationResponse(false, message, availableQuantity, true, flashSaleStock, maxPurchasePerUser);
    }
}
