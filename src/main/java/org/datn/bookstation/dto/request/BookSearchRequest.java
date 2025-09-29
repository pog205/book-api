package org.datn.bookstation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookSearchRequest {
    private Integer bookId;
    private String bookName;
    private BigDecimal price;
    private List<String> images;
    private String coverImageUrl;

}
