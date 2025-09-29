package org.datn.bookstation.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.datn.bookstation.entity.enums.AddressType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressRequest {
    Integer userId;
    String recipientName;
    String addressDetail;
    String phoneNumber;
    String provinceName;
    Integer provinceId;
    String districtName;
    Integer districtId;
    String wardName;
    String wardCode;
    Boolean isDefault;

    AddressType addressType;
} 