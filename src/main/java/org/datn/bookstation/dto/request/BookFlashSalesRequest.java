package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookFlashSalesRequest {
    private Integer bookId;
    private String bookName;
    private BigDecimal price;
    private Integer stockQuantity;

}
