package org.datn.bookstation.dto.request;

import lombok.*;
import org.datn.bookstation.entity.enums.RoleName;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRetail {
    private String fullName;
    private String phoneNumber;
}
