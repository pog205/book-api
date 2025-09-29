package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublisherRequest {
    
    private Integer id; // For update operations
    
    @NotNull(message = "Tên nhà xuất bản không được để trống")
    @Size(max = 255, message = "Tên nhà xuất bản không được vượt quá 255 ký tự")
    private String publisherName;
    
    @Size(max = 2000, message = "Địa chỉ không được vượt quá 2000 ký tự")
    private String address;
    
    // Hỗ trợ cả 2 field name từ frontend
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @JsonProperty("phone")
    private String phoneNumber;
    
    @Size(max = 100, message = "Tên người liên hệ không được vượt quá 100 ký tự")
    private String contactName;
    
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @Size(max = 255, message = "Website không được vượt quá 255 ký tự")
    private String website;
    
    private Integer establishedYear;
    
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    private String description;
    
    private String status;

    private String createdBy; // Tự động gán

    private String updatedBy; // Tự động gán
}
