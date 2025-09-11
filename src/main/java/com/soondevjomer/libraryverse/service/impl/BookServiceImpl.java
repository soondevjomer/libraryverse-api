package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.dto.BookDto;
import com.soondevjomer.libraryverse.model.Book;
import com.soondevjomer.libraryverse.repository.BookRepository;
import com.soondevjomer.libraryverse.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;


    @Override
    public List<BookDto> getAllBooks() {
        return bookRepository.findAll().stream().map(
                book -> BookDto.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .build()
        ).toList();
    }

    @Override
    public BookDto getBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found."));

        return BookDto.builder().id(book.getId()).title(book.getTitle()).build();
    }

    @Override
    public BookDto saveBook(BookDto bookDto) {

        Book book = Book.builder()
                .title(bookDto.getTitle())
                .build();
        Book savedBook = bookRepository.save(book);

        return BookDto.builder()
                .id(savedBook.getId())
                .title(savedBook.getTitle())
                .build();
    }

    @Override
    public BookDto updateBookById(Long bookId, BookDto bookDto) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found."));

        book.setTitle(bookDto.getTitle());
        Book updatedBook = bookRepository.save(book);

        return BookDto.builder()
                .id(updatedBook.getId())
                .title(updatedBook.getTitle())
                .build();
    }

    @Override
    public void deleteBook(Long bookId) {
        bookRepository.deleteById(bookId);
    }
}
