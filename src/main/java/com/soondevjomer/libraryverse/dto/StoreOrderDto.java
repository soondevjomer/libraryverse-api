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
public class StoreOrderDto {
    private Long id;

    @JsonProperty("orderItems")
    private List<OrderItemDto> orderItemDtos;
    private BigDecimal subtotal;
    private String orderStatus;
    private LocalDateTime orderDate;
    private String paymentStatus;
}
