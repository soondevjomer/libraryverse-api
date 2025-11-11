package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.dto.*;
import com.soondevjomer.libraryverse.mapper.LibraryMapper;
import com.soondevjomer.libraryverse.model.Library;
import com.soondevjomer.libraryverse.model.User;
import com.soondevjomer.libraryverse.repository.BookRepository;
import com.soondevjomer.libraryverse.repository.LibraryRepository;
import com.soondevjomer.libraryverse.repository.UserRepository;
import com.soondevjomer.libraryverse.service.ImageService;
import com.soondevjomer.libraryverse.service.LibraryService;
import com.soondevjomer.libraryverse.utils.LibrarySpecification;
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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final LibraryMapper libraryMapper;
    private final ImageService imageService;

    @Override
    public LibraryDto createLibrary(LibraryDto libraryDto) {
        log.info("Creating library with name of {}", libraryDto.getName());

        Library library = libraryMapper.toEntity(libraryDto);
        Library savedLibrary = libraryRepository.save(library);

        return libraryMapper.toDto(savedLibrary);
    }

    @Override
    public LibraryStatDto getLibraryStat() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);

        if (optionalLibrary.isEmpty()) {
            // Return safe default stats for a new library
            return LibraryStatDto.builder()
                    .totalBooks(0)
                    .totalLowOnStock(0)
                    .build();
        }

        Library library = optionalLibrary.get();

        long totalBooks = bookRepository.countByLibrary(library);
        long lowStockBooks = bookRepository.countLowStockBooks(library, 0);

        return LibraryStatDto.builder()
                .totalBooks(totalBooks)
                .totalLowOnStock(lowStockBooks)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageModel<List<LibraryDto>> getLibraryByPage(FilterDto filterDto) {
        log.info("Searching for libraries");

        var spec = LibrarySpecification.filterLibraries(filterDto);
        var pageable = PageRequest.of(filterDto.getPage(), filterDto.getSize());

        log.info("Find al from rep with spec...");
        Page<Library> libraryPage = libraryRepository.findAll(spec, pageable);

        List<LibraryDto> libraryDtos = libraryPage.map(libraryMapper::toDto).toList();

        return new PageModel<>(
                libraryDtos,
                libraryPage.getNumber(),
                libraryPage.getSize(),
                libraryPage.getTotalElements(),
                libraryPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<LibraryDto> getLibraryById(Long libraryId) {
        String username = getCurrentUser();

        Optional<Library> optionalLibrary = libraryRepository.findById(libraryId);
        if (optionalLibrary.isEmpty()) {
            log.info("library is empty or invalid library id that's why get nothing");
            return Optional.empty();
        }

        Library library = optionalLibrary.get();

        if (username!=null) {
            Optional<Library> userLibrary = libraryRepository.findByOwnerUsername(username);
            if (userLibrary.isPresent() && !userLibrary.get().getId().equals(library.getId())) {
                log.info("Increase view count of this library {}", library.getName());
                library.setViewCount(Optional.ofNullable(library.getViewCount()).orElse(0L) + 1);
                library = libraryRepository.save(library);
            }
        }

        log.info("give library by library id");
        return Optional.of(libraryMapper.toDto(library));
    }

    @Override
    public LibraryDto updateLibraryById(Long libraryId, LibraryRequestDto libraryRequestDto, MultipartFile file) {
        log.info("request to update library...");

        log.info("check if the requester owns this library {}", libraryId);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Library existing = libraryRepository.findByOwnerUsername(username)
                .filter(lib -> lib.getId().equals(libraryId))
                .orElseThrow(() -> new AccessDeniedException("You are not authorized to update this library with ID: " + libraryId));

        if (file != null && !file.isEmpty()) {
            UploadDto uploadDto = imageService.uploadLibraryCover(
                    file,
                    existing.getName(),
                    existing.getId()
            );
            existing.setLibraryCover(uploadDto.getFileUrl());
            existing.setLibraryThumbnailCover(uploadDto.getThumbnailFileUrl());
        }

        log.info("proceed updating library...");
        Library updatedLibrary = libraryRepository.save(libraryMapper.mergeLibrary(existing, libraryRequestDto));

        log.info("return library dto");
        return libraryMapper.toDto(updatedLibrary);
    }

    @Override
    public LibraryInfoDto getLibraryInfo(Long libraryId) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // Now check if requester have existed library
        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);
        if (optionalLibrary.isEmpty()) {
            log.info("No library found");
            return LibraryInfoDto.builder().libraryDto(null).build();
        }

        // Now check if requester owns the requested library
        if (optionalLibrary.get().getId().equals(libraryId)) {
            log.info("Requester owns this library");

            return LibraryInfoDto.builder()
                    .libraryDto(libraryMapper.toDto(optionalLibrary.get()))
                    .build();
        }

        return LibraryInfoDto.builder().libraryDto(null).build();
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return  (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : null;
    }
}
