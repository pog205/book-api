package org.datn.bookstation.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    private BigDecimal totalSpent;
    private Integer totalPoint;
    private Integer roleId;
    private String roleName;
}
