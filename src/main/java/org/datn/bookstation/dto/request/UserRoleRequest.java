package org.datn.bookstation.dto.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class UserRoleRequest {
    private Integer id;
    private String fullName;
    private String phoneNumber;
    private Integer roleId;
}
