package org.datn.bookstation.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRepuest {
    private Integer id;
    private String supplierName;
    private String contactName;
    private String phoneNumber;
    private String email;
    private String address;
    private Byte status;
    private String createdBy;
    private String updatedBy;
        private Long createdAt;
        private Long updatedAt;
}
