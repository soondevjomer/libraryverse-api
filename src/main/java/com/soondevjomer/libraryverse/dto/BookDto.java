package com.soondevjomer.libraryverse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookDto {

    private Long id;
    private String isbn;

    private Long libraryId;

    private LocalDateTime createdDate;

    @JsonProperty("bookDetail")
    private BookDetailDto bookDetail;

    @JsonProperty("inventory")
    private InventoryDto inventory;

    private double popularityScore;

    private double roundedRating;
}
