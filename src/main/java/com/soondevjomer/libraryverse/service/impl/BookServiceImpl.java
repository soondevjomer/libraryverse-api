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
    private final InventoryService inventoryService;
    private final BookMapper bookMapper;
    private final InventoryRepository inventoryRepository;
    private final ImageService imageService;

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
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);

        optionalLibrary.ifPresent((library -> {
            if (!library.getId().equals(book.getLibrary().getId())) {
                log.info("The viewer is not own by the user so add view count");
                book.setViewCount(book.getViewCount() + 1);
            }
        }));

        book.setViewCount(book.getViewCount() + 1);
        Book savedBook = bookRepository.save(book);
        return bookMapper.toDto(savedBook);
    }

    @Transactional
    @Override
    public BookDto updateBookById(Long bookId, BookDto bookDto, MultipartFile file) {
        Book existing = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));

        User currentUser = getCurrentUser();
        Library userLibrary = libraryRepository.findByOwnerUsername(currentUser.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Library not found"));

        if (!existing.getLibrary().getId().equals(userLibrary.getId())) {
            throw new AccessDeniedException("You are not allowed to modify this book");
        }

        if (bookDto.getIsbn() != null) {
            existing.setIsbn(bookDto.getIsbn());
        }

        if (existing.getInventory() != null && bookDto.getInventory() != null) {
            existing.getInventory().setAvailableStock(bookDto.getInventory().getAvailableStock());
        }

        if (bookDto.getBookDetail() != null) {
            BookDetail mergedDetail = bookMapper.mergeBookDetail(existing.getBookDetail(), bookDto.getBookDetail());
            if (mergedDetail.getAuthors() != null && !(mergedDetail.getAuthors() instanceof java.util.ArrayList)) {
                mergedDetail.setAuthors(new java.util.ArrayList<>(mergedDetail.getAuthors()));
            }
            if (mergedDetail.getGenres() != null && !(mergedDetail.getGenres() instanceof java.util.ArrayList)) {
                mergedDetail.setGenres(new java.util.ArrayList<>(mergedDetail.getGenres()));
            }
            existing.setBookDetail(mergedDetail);
            if (bookDto.getBookDetail().getQuantity() != null) {
                inventoryService.updateStock(existing, bookDto.getBookDetail().getQuantity());
            }
        }
        log.info("is file null?: {}", file);
        if (file != null && !file.isEmpty()) {
            try {
                UploadDto uploadDto = imageService.uploadBookCover(
                        file,
                        existing.getBookDetail().getTitle(),
                        existing.getId()
                );
                existing.getBookDetail().setBookCover(uploadDto.getFileUrl());
                existing.getBookDetail().setBookThumbnailCover(uploadDto.getThumbnailFileUrl());
            } catch (Exception e) {
                log.error("Image upload failed, rolling back book cover creation: {}", e.getMessage());
                imageService.deleteImageFolder("book-covers", existing.getId());
                log.info("Failed to upload book cover, cause of {}", e.getMessage());
            }
        }

        Book updatedBook = bookRepository.save(existing);
        return bookMapper.toDto(updatedBook);
    }

    @Transactional
    @Override
    public BookDto createBookToLibrary(BookDto bookDto, MultipartFile file) {
        log.info("Creating book in library");
        User currentUser = getCurrentUser();
        Library library = libraryRepository.findByOwnerUsername(currentUser.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Library not found"));

        Book book = bookMapper.toEntity(bookDto);
        book.setViewCount(0L);
        book.setLibrary(library);

        Inventory inventory = Inventory.builder()
                .availableStock(bookDto.getBookDetail().getQuantity() != null ? bookDto.getBookDetail().getQuantity() : 0)
                .reservedStock(0)
                .delivered(0)
                .shipped(0)
                .build();
        book.setInventory(inventory);
        Book saved = bookRepository.save(book);

        if (file != null && !file.isEmpty()) {
            try {
                UploadDto uploadDto = imageService.uploadBookCover(
                        file,
                        saved.getBookDetail().getTitle(),
                        saved.getId()
                );
                saved.getBookDetail().setBookCover(uploadDto.getFileUrl());
                saved.getBookDetail().setBookThumbnailCover(uploadDto.getThumbnailFileUrl());
            } catch (Exception e) {
                log.error("Image upload failed, rolling back book creation: {}", e.getMessage());
                imageService.deleteImageFolder("book-covers", book.getId());
                throw new RuntimeException("Failed to upload book cover");
            }
        }

        return bookMapper.toDto(bookRepository.save(saved));
    }

    @Override
    public BookDto copyBookToLibrary(BookDto bookDto, MultipartFile file) {
        log.info("Copying existing book into library");

        User currentUser = getCurrentUser();
        Library library = libraryRepository.findByOwnerUsername(currentUser.getUsername())
                .orElseThrow(() -> new NoSuchElementException("Library not found"));

        Book copyBook = bookMapper.toEntity(bookDto);
        copyBook.setId(null);
        copyBook.setLibrary(library);
        copyBook.setViewCount(0L);

        Inventory inventory = Inventory.builder()
                .availableStock(bookDto.getBookDetail().getQuantity() != null ? bookDto.getBookDetail().getQuantity() : 0)
                .reservedStock(0)
                .delivered(0)
                .shipped(0)
                .build();
        copyBook.setInventory(inventory);

        Book savedCopy = bookRepository.save(copyBook);
        try {
            if (file != null && !file.isEmpty()) {
                log.info("New image provided, uploading fresh cover...");
                UploadDto uploadDto = imageService.uploadBookCover(
                        file,
                        savedCopy.getBookDetail().getTitle(),
                        savedCopy.getId()
                );
                savedCopy.getBookDetail().setBookCover(uploadDto.getFileUrl());
                savedCopy.getBookDetail().setBookThumbnailCover(uploadDto.getThumbnailFileUrl());

            } else if (bookDto.getBookDetail() != null &&
                    bookDto.getBookDetail().getBookCover() != null &&
                    bookDto.getBookDetail().getBookThumbnailCover() != null) {

                log.info("No new file uploaded — copying existing cover...");
                UploadDto copyUpload = imageService.copyImageFromExisting(
                        bookDto.getBookDetail().getBookCover(),
                        bookDto.getBookDetail().getBookThumbnailCover(),
                        "book-covers",
                        savedCopy.getId(),
                        savedCopy.getBookDetail().getTitle()
                );
                savedCopy.getBookDetail().setBookCover(copyUpload.getFileUrl());
                savedCopy.getBookDetail().setBookThumbnailCover(copyUpload.getThumbnailFileUrl());
            } else {
                log.warn("No image found in original book — skipping image copy.");
            }

        } catch (Exception e) {
            log.error("Book copy image handling failed: {}", e.getMessage(), e);
            imageService.deleteImageFolder("book-covers", savedCopy.getId());
        }

        return bookMapper.toDto(bookRepository.save(savedCopy));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

}
