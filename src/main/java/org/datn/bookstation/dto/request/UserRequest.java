package org.datn.bookstation.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserRequest {
    private String full_name;
    private String email;
    private String phone_number;
    private Integer role_id;
    private String status;
    private BigDecimal total_spent;
    private Integer total_point;
}
