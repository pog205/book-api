package org.datn.bookstation.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserResponse {
    private Integer user_id;
    private String full_name;
    private String email;
    private String phone_number;
    private Integer role_id;
    private String role_name; // Thêm tên vai trò
    private Byte status;
    private Byte email_verified; // Trạng thái xác nhận email: 0 = chưa xác nhận, 1 = đã xác nhận
    private String created_at;
    private String updated_at;
    private BigDecimal total_spent;
    private Integer total_point;
    private Byte isRetail;

}
