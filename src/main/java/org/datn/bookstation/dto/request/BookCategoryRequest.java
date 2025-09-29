package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookCategoryRequest {
    private Integer bookId;
    private String bookName;
    private String description;
    private BigDecimal price;
    private Integer categoryId;
}
