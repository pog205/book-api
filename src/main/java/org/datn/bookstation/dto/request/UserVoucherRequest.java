package org.datn.bookstation.dto.request;

import lombok.Data;


@Data
public class UserVoucherRequest {
    private Integer userId;
    private Integer voucherId;

}