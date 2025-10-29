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
public class StoreOrderSummaryDto {

    private Long storeOrderId;
    private String libraryName;
    private BigDecimal subtotal;
    private String status;
}
