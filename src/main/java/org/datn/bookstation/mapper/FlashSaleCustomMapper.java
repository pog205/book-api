package org.datn.bookstation.mapper;

import org.datn.bookstation.dto.response.FlashSaleDisplayResponse;
import org.datn.bookstation.entity.FlashSale;
import org.springframework.stereotype.Component;

@Component
public class FlashSaleCustomMapper {
    public FlashSaleDisplayResponse toDisplayResponse(FlashSale flashSale) {
        if (flashSale == null)
            return null;

        long remainingMillis = flashSale.getEndTime() - System.currentTimeMillis();
        if (remainingMillis < 0)
            remainingMillis = 0;
        
        return FlashSaleDisplayResponse.builder()
                .name(flashSale.getName())
                .remainingMillis(remainingMillis)
                .build();
    }
}