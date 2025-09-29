package org.datn.bookstation.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class ParentCategoryResponse {
    private Integer id;
    private String categoryName;
    private String description;
    private Byte status;
    private List<ParentCategoryResponse> parentCategory = new ArrayList<>();
}
