package com.soondevjomer.libraryverse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class OrderResponseDto {
    private Long orderId;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private BigDecimal totalAmount;

    @JsonProperty("storeOrders")
    private List<StoreOrderDto> storeOrderDtos;
}
