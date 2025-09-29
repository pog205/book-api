package org.datn.bookstation.dto.response;

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
public class AddressResponse {
    Integer id;
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
    Long createdAt;
    Long updatedAt;
    Integer createdBy;
    Integer updatedBy;
    Byte status;

    AddressType addressType;
} 