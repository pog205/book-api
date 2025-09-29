package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.datn.bookstation.entity.enums.RoleName;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenValidationResponse {
    private Boolean valid;
    private Integer userId;
    private String email;
    private String fullName;
    private String role;
    private Byte status;
    private Byte emailVerified;
    private String phoneNumber;
}
