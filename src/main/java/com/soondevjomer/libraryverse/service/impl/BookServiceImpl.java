package com.soondevjomer.libraryverse.service.impl;

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
        log.info("Incrementing view count for book {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
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

        if (file != null && !file.isEmpty()) {
            String oldCoverUrl = existing.getBookDetail().getBookCover();
            log.info("old Cover Url {}", oldCoverUrl);
            if (oldCoverUrl != null && !oldCoverUrl.isEmpty()) {
                imageService.deleteImageFile(oldCoverUrl);
            }
            UploadDto upload = imageService.uploadBookCover(
                    file,
                    existing.getBookDetail().getTitle(),
                    existing.getId()
            );
            existing.getBookDetail().setBookCover(upload.getFileUrl());
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
            UploadDto uploadDto = imageService.uploadBookCover(
              file,
              saved.getBookDetail().getTitle(),
              saved.getId()
            );
            saved.getBookDetail().setBookCover(uploadDto.getFileUrl());
            bookRepository.save(saved);
        }

        return bookMapper.toDto(saved);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private Book moveBookCoverIfTemporary(Book book) {
        String coverUrl = book.getBookDetail().getBookCover();
        if (coverUrl == null || !coverUrl.contains("/temp/")) return null;

        try {
            log.info("Moving temp book cover for book {}", book.getId());

            String projectDir = System.getProperty("user.dir");
            String relativeUploadPath = coverUrl.replace("/files/", "uploads/");
            Path tempPath = Paths.get(projectDir, relativeUploadPath).normalize();

            if (!Files.exists(tempPath)) {
                log.warn("Temp cover not found at {}", tempPath);
                return null;
            }

            Path bookDir = Paths.get(projectDir, "uploads", "book-covers", String.valueOf(book.getId()));
            Files.createDirectories(bookDir);

            // Delete any existing files in destination folder (ensure single file per folder)
            try (var stream = Files.list(bookDir)) {
                stream.forEach(existing -> {
                    try {
                        Files.deleteIfExists(existing);
                        log.info("Deleted existing cover in destination: {}", existing);
                    } catch (IOException ex) {
                        log.warn("Failed to delete existing file {} : {}", existing, ex.getMessage());
                    }
                });
            } catch (IOException ignored) {}

            Path newPath = bookDir.resolve(tempPath.getFileName());

            try {
                Files.move(tempPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (UnsupportedOperationException | IOException ex) {
                log.warn("Files.move failed ({}). Trying copy+delete.", ex.toString());
                Files.copy(tempPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(tempPath);
            }

            String newUrl = "/files/book-covers/" + book.getId() + "/" + tempPath.getFileName();
            book.getBookDetail().setBookCover(newUrl);
            log.info("Book cover moved to {}", newUrl);

            // Remove temp folder if empty (the UUID folder)
            Path tempFolder = tempPath.getParent();
            if (tempFolder != null && Files.isDirectory(tempFolder)) {
                try (var files = Files.list(tempFolder)) {
                    if (files.findAny().isEmpty()) {
                        Files.delete(tempFolder);
                        log.info("Deleted empty temp folder: {}", tempFolder);
                    }
                } catch (IOException ignored) {}
            }

            return bookRepository.save(book);

        } catch (Exception e) {
            log.error("Failed to move book cover for book {}: {}", book.getId(), e.getMessage(), e);
        }
        return null;
    }

}
