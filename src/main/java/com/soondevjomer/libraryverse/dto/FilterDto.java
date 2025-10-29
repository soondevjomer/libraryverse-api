package com.soondevjomer.libraryverse.dto;

import com.soondevjomer.libraryverse.constant.SortBy;
import com.soondevjomer.libraryverse.constant.SortDirection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterDto {
    private Integer page;
    private Integer size;
    private String sortField;
    private String sortOrder;
    private String search;
    private Integer libraryId;

    @Enumerated(EnumType.STRING)
    private SortBy sortBy;

    @Enumerated(EnumType.STRING)
    private SortDirection sortDirection;
}
