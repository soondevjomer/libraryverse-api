package com.soondevjomer.libraryverse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageModel<T> {

    private T content;
    private int pageNumber; // current page number
    private int pageSize; // no of items per page
    private long totalElement;
    private int totalPage;
}
