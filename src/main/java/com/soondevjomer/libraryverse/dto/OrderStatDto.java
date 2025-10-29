package com.soondevjomer.libraryverse.dto;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatDto {
    private Integer totalDelivered;
    private Integer totalPending;
    private Integer totalShipped;
}
