package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRankSimpleResponse {
    private Integer id;
    private String userEmail;
    private String userName;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
}
