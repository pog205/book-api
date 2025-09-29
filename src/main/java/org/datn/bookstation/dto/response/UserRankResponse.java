package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRankResponse {
    private Integer id;
    private Integer userId;
    private String userEmail;
    private Integer rankId;
    private String rankName;
    private Byte status;
}
