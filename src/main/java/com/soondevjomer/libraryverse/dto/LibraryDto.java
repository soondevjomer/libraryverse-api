package com.soondevjomer.libraryverse.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.soondevjomer.libraryverse.model.Book;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibraryDto {

    private Long id;
    private String name;
    private String address;
    private String description;
    private String contactNumber;
    private String libraryCover;

    private String ownerName;

    private double popularityScore;
}
