package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublisherResponse {
    private Integer id;
    private String publisherName;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
    private Integer establishedYear;
    private String description;
    private Byte status;
    private Long createdAt;
    private Long updatedAt;
    private Integer createdBy;
    private Integer updatedBy;
}
