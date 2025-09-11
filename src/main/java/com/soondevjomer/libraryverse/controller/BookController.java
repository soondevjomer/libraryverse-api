package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.dto.BookDto;
import com.soondevjomer.libraryverse.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBookById(@PathVariable Long bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

}
