package org.datn.bookstation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookStockResponse {
    private String bookName;
    private Integer stockQuantity;
}