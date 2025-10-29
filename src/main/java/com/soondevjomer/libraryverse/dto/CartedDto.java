package com.soondevjomer.libraryverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartedDto {

    private Long cartId;
    private Long bookId;
    private String bookName;
    private BigDecimal price;
    private Integer quantity;
    private Integer maxQuantity;
}
