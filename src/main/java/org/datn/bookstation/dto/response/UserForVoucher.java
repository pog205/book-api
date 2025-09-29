package org.datn.bookstation.dto.response;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserForVoucher {

    private Integer id;
    private Integer userId;
    private String fullName;
    private Integer voucherId;
    private String voucherCode;
    private Integer usedCount;
    private Long createdAt;

}
