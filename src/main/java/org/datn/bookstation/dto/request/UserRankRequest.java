package org.datn.bookstation.dto.request;

import lombok.Data;

@Data
public class UserRankRequest {
    private Integer userId;
    private Integer rankId;
    private Byte status;
}
