package com.soondevjomer.libraryverse.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonRootName("libraryStat")
public class LibraryStatDto {

    private long totalBooks;
    private long totalLowOnStock;
}
