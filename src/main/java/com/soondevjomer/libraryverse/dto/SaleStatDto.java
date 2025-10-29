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
public class SaleStatDto {
    private BigDecimal totalRevenue;
    private Integer totalPurchases;
    private String topBook;
    private String topAuthor;
    private String topGenre;
}
