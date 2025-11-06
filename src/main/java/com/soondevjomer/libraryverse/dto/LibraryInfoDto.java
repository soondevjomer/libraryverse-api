package com.soondevjomer.libraryverse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibraryInfoDto {

    @JsonProperty("library")
    private LibraryDto libraryDto;
}
