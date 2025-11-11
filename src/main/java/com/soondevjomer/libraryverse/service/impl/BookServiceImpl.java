package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.constant.Role;
import com.soondevjomer.libraryverse.dto.BookDto;
import com.soondevjomer.libraryverse.dto.FilterDto;
import com.soondevjomer.libraryverse.dto.PageModel;
import com.soondevjomer.libraryverse.dto.UploadDto;
import com.soondevjomer.libraryverse.mapper.BookMapper;
import com.soondevjomer.libraryverse.model.*;
import com.soondevjomer.libraryverse.repository.*;
import com.soondevjomer.libraryverse.service.*;
import com.soondevjomer.libraryverse.utils.BookSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;
    private final ImageService imageService;
    private final CloudinaryService cloudinaryService;


    @Override
    public PageModel<List<BookDto>> getBookByPage(FilterDto filterDto) {
        log.info("Searching for books...");
        var spec = BookSpecification.filterBooks(filterDto);
        var pageable = PageRequest.of(filterDto.getPage(), filterDto.getSize());
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);
        List<BookDto> bookDtos = bookPage.map(bookMapper::toDto).toList();
        return new PageModel<>(
                bookDtos,
                bookPage.getNumber(),
                bookPage.getSize(),
                bookPage.getTotalElements(),
                bookPage.getTotalPages()
        );
    }

    @Transactional
    @Override
    public BookDto getBookById(Long bookId) {
        log.info("#BookService->getBookById: Get book by id");
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            return new BookDto();
        }

        Book book = optionalBook.get();

        String username = getCurrentUserUsername();
        if (username==null) {
            log.info("#BookService->getBookById: Anonymous is requesting for book {}", book.getBookDetail().getTitle());
            return bookMapper.toDto(book);
        }
        // Username has value then requester is a logged-in user
        log.info("#BookService->getBookById: User {} is requesting for book {}", username, book.getBookDetail().getTitle());

        // Now check if the logged user doesn't own the book so it doesn't increase its view count
        Optional<User> optionalUser = userRepository.findByUsername(username);

        optionalUser.ifPresent((user)->{
            log.info("#BookService->getBookById: Proceed adding book view count");
            if (!book.getLibrary().getOwner().equals(user)) {
                log.info("#BookService->getBookById: The request is not the owner add view count");
                book.setViewCount(book.getViewCount() + 1);
            }
        });

        log.info("#BookService->getBookById: Returning requested book");
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Transactional
    @Override
    public BookDto updateBookById(Long bookId, BookDto bookDto, MultipartFile file) {
        log.info("#BookService->updateBookById: Update book by id");

        // Check first if the given data is not empty and existed
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            log.info("#BookService->updateBookById: Book does not exist");
            return new BookDto();
        }

        Book existingBook = optionalBook.get();

        // Book existed now check if the requester is the owner
        String username = getCurrentUserUsername();
        if (username==null) {
            log.info("#BookService->updateBookById: Book does not exist");
            return new BookDto();
        }

        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);
        if (optionalLibrary.isEmpty()) {
            log.info("#BookService->updateBookById: Library does not exist");
            return new BookDto();
        }

        Library existingLibrary = optionalLibrary.get();
        // Now there is existed user and library
        if (!existingBook.getLibrary().equals(existingLibrary)) {
            log.info("#BookService->updateBookById: User not allowed to edit this book");
            return new BookDto();
        }

        // Proceed updating
        log.info("#BookService->updateBookById: Updating book is in process");
        if (bookDto.getIsbn() != null) {
            existingBook.setIsbn(bookDto.getIsbn());
        }

        if (existingBook.getInventory() != null && bookDto.getInventory() != null) {
            existingBook.getInventory().setAvailableStock(bookDto.getInventory().getAvailableStock());
        }

        if (bookDto.getBookDetail() != null) {
            BookDetail mergedDetail = bookMapper.mergeBookDetail(existingBook.getBookDetail(), bookDto.getBookDetail());
            if (mergedDetail.getAuthors() != null && !(mergedDetail.getAuthors() instanceof java.util.ArrayList)) {
                mergedDetail.setAuthors(new java.util.ArrayList<>(mergedDetail.getAuthors()));
            }
            if (mergedDetail.getGenres() != null && !(mergedDetail.getGenres() instanceof java.util.ArrayList)) {
                mergedDetail.setGenres(new java.util.ArrayList<>(mergedDetail.getGenres()));
            }
            existingBook.setBookDetail(mergedDetail);
        }
        if (file != null && !file.isEmpty()) {
            log.info("#BookService->updateBookById: File is not empty");
            UploadDto uploadDto = imageService.uploadBookCover(
                    file,
                    existingBook.getBookDetail().getTitle(),
                    existingBook.getId());
            existingBook.getBookDetail().setBookCover(uploadDto.getFileUrl());
            existingBook.getBookDetail().setBookThumbnailCover(uploadDto.getThumbnailFileUrl());
        }

        Book updatedBook = bookRepository.save(existingBook);
        log.info("#BookService->updateBookById: Updated book view count {}", updatedBook.getViewCount());
        return bookMapper.toDto(updatedBook);
    }

    @Transactional
    @Override
    public BookDto createBookToLibrary(BookDto bookDto, MultipartFile file) {
        log.info("#BookService->createBookToLibrary: Creating book to library");

        String username = getCurrentUserUsername();
        if (username==null) {
            log.info("#BookService->createBookToLibrary: Anonymous|Unauthenticated User is making a request");
            return new BookDto();
        }

        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);
        if (optionalLibrary.isEmpty()) {
            log.info("#BookService->createBookToLibrary: No library found stop the request");
            return new BookDto();
        }

        // Proceed on creating book to library
        Library existingLibrary = optionalLibrary.get();
        Book book = bookMapper.toEntity(bookDto);
        book.setLibrary(existingLibrary);
        book.setViewCount(0L);

        // Create an empty inventory
        Inventory inventory = Inventory.builder()
                .availableStock(bookDto.getBookDetail().getQuantity() != null ? bookDto.getBookDetail().getQuantity() : 0)
                .reservedStock(0)
                .delivered(0)
                .shipped(0)
                .build();
        book.setInventory(inventory);
        Book saved = bookRepository.save(book);
        log.info("#BookService->createBookToLibrary: Book was saved");

        if (file != null && !file.isEmpty()) {
            log.info("#BookService->createBookToLibrary: File is not empty");
            UploadDto uploadDto = imageService.uploadBookCover(
                    file,
                    saved.getBookDetail().getTitle(),
                    saved.getId());
            saved.getBookDetail().setBookCover(uploadDto.getFileUrl());
            saved.getBookDetail().setBookThumbnailCover(uploadDto.getThumbnailFileUrl());
        }

        log.info("#BookService->createBookToLibrary: Creating book to library done");
        return bookMapper.toDto(bookRepository.save(saved));
    }

    @Transactional
    @Override
    public BookDto copyBookToLibrary(BookDto bookDto, MultipartFile file) {
        log.info("#BookService->copyBookToLibrary: Save copied book");

        String username = getCurrentUserUsername();
        if (username==null) {
            log.info("#BookService->copyBookToLibrary: Anonymous|Unauthenticated User is making a request");
            return new BookDto();
        }

        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);
        if (optionalLibrary.isEmpty()) {
            log.info("#BookService->copyBookToLibrary: No library found stop the request");
            return new BookDto();
        }

        Library existingLibrary = optionalLibrary.get();

        Book copyBook = bookMapper.toEntity(bookDto);
        copyBook.setId(null); // to make it feel new
        copyBook.setLibrary(existingLibrary);
        copyBook.setViewCount(0L);

        Inventory inventory = Inventory.builder()
                .availableStock(bookDto.getBookDetail().getQuantity() != null ? bookDto.getBookDetail().getQuantity() : 0)
                .reservedStock(0)
                .delivered(0)
                .shipped(0)
                .build();
        copyBook.setInventory(inventory);
        Book savedCopy = bookRepository.save(copyBook);
        log.info("#BookService->copyBookToLibrary: Saved copied book");

        if (savedCopy.getBookDetail().getBookCover()!=null && savedCopy.getBookDetail().getBookThumbnailCover()!=null) {
            // Then book has already existed file then just copy that
            log.info("#BookService->copyBookToLibrary: There is already existed book cover");
            UploadDto uploadDto = cloudinaryService.copyImageFromExisting(
                    savedCopy.getBookDetail().getBookCover(),
                    savedCopy.getBookDetail().getBookThumbnailCover(),
                    "book-covers",
                    savedCopy.getId(),
                    savedCopy.getBookDetail().getTitle()
            );
            if (uploadDto!=null) {
                savedCopy.getBookDetail().setBookCover(uploadDto.getFileUrl());
                savedCopy.getBookDetail().setBookThumbnailCover(uploadDto.getThumbnailFileUrl());
            }
        }

        if (file != null && !file.isEmpty()) {
            log.info("#BookService->copyBookToLibrary: File is not null");
            UploadDto uploadDto = imageService.uploadBookCover(
                    file,
                    savedCopy.getBookDetail().getTitle(),
                    savedCopy.getId()
            );
            savedCopy.getBookDetail().setBookCover(uploadDto.getFileUrl());
            savedCopy.getBookDetail().setBookThumbnailCover(uploadDto.getThumbnailFileUrl());
        }

        return bookMapper.toDto(bookRepository.save(savedCopy));
    }

    private String getCurrentUserUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return  (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : null;
    }

}
