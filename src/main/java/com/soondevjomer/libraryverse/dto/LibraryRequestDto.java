package com.soondevjomer.libraryverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibraryRequestDto {
    private String name;
    private String address;
    private String contactNumber;
    private String description;
    private String libraryCover;
}
