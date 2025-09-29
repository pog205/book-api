package org.datn.bookstation.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleDisplayResponse {
    String name;
    // FlashSaleDisplayResponse.java
 long remainingMillis;


}
