package com.soondevjomer.libraryverse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soondevjomer.libraryverse.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyCartDto {

    private Long libraryId;
    private String libraryName;
    private List<CartedDto> carts;
}
