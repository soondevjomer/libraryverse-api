package com.soondevjomer.libraryverse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soondevjomer.libraryverse.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibrarianSaleDto {
    private Long id;
    private LocalDateTime orderDate;
    private String customerName;

    @JsonProperty("saleItems")
    private List<SaleItemDto> items;
    private BigDecimal subtotal;
    private OrderStatus orderStatus;
}
