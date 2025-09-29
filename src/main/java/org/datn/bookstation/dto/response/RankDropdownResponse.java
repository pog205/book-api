package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankDropdownResponse {
    private Integer id;
    private String name;
}

// Đã chuyển sang dùng DropdownOptionResponse để dùng chung cho các controller khác
// File này không còn cần thiết, có thể xoá nếu không dùng ở đâu khác.
