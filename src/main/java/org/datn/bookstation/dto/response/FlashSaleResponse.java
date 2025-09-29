package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashSaleResponse {
    Integer id;

    String name;

    Long startTime;

    Long endTime;

    Byte status;

    Long createdAt;

    Long updatedAt;

    Long createdBy;

    Long updatedBy;
}
