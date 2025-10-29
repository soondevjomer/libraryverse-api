package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.BookDto;
import com.soondevjomer.libraryverse.dto.FilterDto;
import com.soondevjomer.libraryverse.dto.PageModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {

    PageModel<List<BookDto>> getBookByPage(FilterDto filterDto);

    BookDto getBookById(Long bookId);

    BookDto updateBookById(Long bookId, BookDto bookDto, MultipartFile file);

    BookDto createBookToLibrary(BookDto bookDto, MultipartFile file);


}
