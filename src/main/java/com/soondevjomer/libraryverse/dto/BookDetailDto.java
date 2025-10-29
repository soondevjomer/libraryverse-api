package com.soondevjomer.libraryverse.dto;

import com.soondevjomer.libraryverse.model.Author;
import com.soondevjomer.libraryverse.model.Genre;
import com.soondevjomer.libraryverse.model.Publisher;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookDetailDto {

    private Long id;
    private String title;
    private String seriesTitle;
    private String description;
    private String bookCover;
    private List<String> genres;
    private List<String> authors;
    private List<String> tags;
    private String publisher;
    private Long publishedYear;
    private BigDecimal price;
    private Integer quantity;
}
