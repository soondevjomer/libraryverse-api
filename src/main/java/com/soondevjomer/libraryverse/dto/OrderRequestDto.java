package com.soondevjomer.libraryverse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soondevjomer.libraryverse.constant.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequestDto {

    private String paymentMethod;

    private String address;
    private String contactNumber;

    @JsonProperty("orderItems")
    private List<OrderItemDto> orderItemDtos;
}
