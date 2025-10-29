package com.soondevjomer.libraryverse.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soondevjomer.libraryverse.constant.SortBy;
import com.soondevjomer.libraryverse.constant.SortDirection;
import com.soondevjomer.libraryverse.dto.BookDto;
import com.soondevjomer.libraryverse.dto.FilterDto;
import com.soondevjomer.libraryverse.dto.PageModel;
import com.soondevjomer.libraryverse.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import java.io.DataInput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping()
    public ResponseEntity<PageModel<List<BookDto>>> getBookByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String sortBy,
            @RequestParam(defaultValue = "") String sortDirection,
            @RequestParam(defaultValue = "0") int libraryId
    ) {
        return ResponseEntity.ok(bookService.getBookByPage(
                FilterDto.builder()
                        .page(page)
                        .size(size)
                        .sortField(sortField)
                        .sortOrder(sortOrder)
                        .search(search)
                        .sortBy(SortBy.valueOf(sortBy))
                        .sortDirection(SortDirection.valueOf(sortDirection))
                        .libraryId(libraryId)
                        .build()
        ));
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBookById(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBookToLibrary(
            @RequestPart(value = "book") BookDto bookDto,
            @RequestPart(value = "file", required = false)MultipartFile file
    ) throws IOException {
        log.info("RAW BOOK JSON: {}", bookDto);
        return new ResponseEntity<>(bookService.createBookToLibrary(bookDto, file), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBookById(
            @PathVariable Long bookId,
            @RequestPart(value = "book") BookDto bookDto,
            @RequestPart(value = "file", required = false)MultipartFile file) {

        log.info("Book Controller trying to update the book with {}", bookDto);
        return ResponseEntity.ok(bookService.updateBookById(bookId, bookDto, file));
    }
}
