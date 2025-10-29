package com.soondevjomer.libraryverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryDto {

    private Long id;

    private Integer availableStock;
    private Integer reservedStock;
    private Integer shipped;
    private Integer delivered;
}
