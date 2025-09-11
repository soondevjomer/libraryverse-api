package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.BookDto;
import com.soondevjomer.libraryverse.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {

    List<BookDto> getAllBooks();

    BookDto getBookById(Long bookId);

    BookDto saveBook(BookDto bookDto);

    BookDto updateBookById(Long bookId, BookDto bookDto);

    void deleteBook(Long bookId);
}
