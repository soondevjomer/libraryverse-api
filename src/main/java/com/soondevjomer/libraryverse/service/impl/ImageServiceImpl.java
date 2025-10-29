package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.dto.UploadDto;
import com.soondevjomer.libraryverse.repository.BookRepository;
import com.soondevjomer.libraryverse.repository.LibraryRepository;
import com.soondevjomer.libraryverse.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final String BOOK_COVER_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/book-covers/";
    private static final String LIBRARY_COVER_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/library-covers/";
    private static final String PROFILE_IMAGE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profile-images/";

    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;

    @Override
    public UploadDto uploadBookCover(MultipartFile file, String bookTitle, Long bookId) {
        log.info("Uploading book cover");
        return uploadImage(
                file,
                bookTitle,
                bookId,
                BOOK_COVER_UPLOAD_DIR + bookId + "/",
                "/files/book-covers/" + bookId + "/"
        );
    }

    @Override
    public UploadDto uploadLibraryCover(MultipartFile file, String libraryName, Long libraryId) {
        log.info("Uploading library cover");
        return uploadImage(
                file,
                libraryName,
                libraryId,
                LIBRARY_COVER_UPLOAD_DIR + libraryId + "/",
                "/files/library-covers/" + libraryId + "/"
        );
    }

    @Override
    public UploadDto uploadProfileImage(MultipartFile file, String username, Long userId) {
        log.info("Uploading profile image");
        return uploadImage(
                file,
                username,
                userId,
                PROFILE_IMAGE_UPLOAD_DIR + userId + "/",
                "/files/profile-images/" + userId + "/"
        );
    }

    @Override
    public String saveTempBookCover(MultipartFile file, Long libraryId) {
        if (file == null || file.isEmpty()) return null;

        try {
            String safeName = "book_" + libraryId + "_" + System.currentTimeMillis();
            String uniqueId = UUID.randomUUID().toString();

            Path tempDir = Paths.get(BOOK_COVER_UPLOAD_DIR, "temp", uniqueId);
            Files.createDirectories(tempDir);

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains(".")) ?
                    originalName.substring(originalName.lastIndexOf(".")) : ".jpg";

            String fileName = safeName + extension;
            Path filePath = tempDir.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/files/book-covers/temp/" + uniqueId + "/" + fileName;
            log.info("Temporary book cover saved at {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("Failed to save temporary book cover: {}", e.getMessage());
            throw new RuntimeException("Failed to save temporary book cover", e);
        }
    }

    private UploadDto uploadImage(MultipartFile file, String name, Long libraryId, String baseDir, String publicPath) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File cannot be empty");

        try {
            String safeName = name.trim().replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase();
            Path folderPath = Paths.get(baseDir);
            Files.createDirectories(folderPath);

            try (var files = Files.list(folderPath)) {
                files.forEach(existing -> {
                    try {
                        Files.deleteIfExists(existing);
                        log.info("Deleted old file {}", existing);
                    } catch (IOException ignored) {
                    }
                });
            }

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains(".")) ?
                    originalName.substring(originalName.lastIndexOf(".")) : ".jpg";

            String fileName = safeName + "_" + System.currentTimeMillis() + extension;
            Path filePath = folderPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = publicPath + fileName;

            return UploadDto.builder()
                    .fileName(fileName)
                    .fileUrl(fileUrl)
                    .folderPath(filePath.toString())
                    .build();

        } catch (IOException e) {
            log.error("Failed to save image: {}", e.getMessage());
            throw new RuntimeException("Failed to save image", e);
        }
    }

    @Override
    public void deleteImageFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            // Candidates to try (normalized)
            String[] candidates = new String[] {
                    fileUrl,
                    fileUrl.startsWith("/") ? fileUrl.substring(1) : "/" + fileUrl // ensure leading slash
            };

            boolean deleted = false;
            for (String candidate : candidates) {
                // map /files/... -> /uploads/...
                String rel = candidate.replaceFirst("^/files", "/uploads");
                Path path = Paths.get(System.getProperty("user.dir"), rel).normalize();

                log.info("Attempting to delete image file candidate: {}", path);

                if (Files.exists(path)) {
                    try {
                        Files.delete(path);
                        log.info("Deleted image file: {}", path);
                    } catch (IOException ex) {
                        log.warn("Failed to delete {} : {}", path, ex.getMessage());
                    }

                    // cleanup parent dir if empty
                    Path parent = path.getParent();
                    if (parent != null && Files.isDirectory(parent)) {
                        try (var s = Files.list(parent)) {
                            if (s.findAny().isEmpty()) {
                                Files.delete(parent);
                                log.info("Deleted empty folder: {}", parent);
                            }
                        } catch (IOException ignored) {}
                    }

                    deleted = true;
                    break;
                } else {
                    log.debug("Candidate not found: {}", path);
                }
            }

            if (!deleted) {
                log.warn("deleteImageFile: no candidate matched for URL [{}]", fileUrl);
            }

        } catch (Exception e) {
            log.error("Failed to delete image: {}", e.getMessage(), e);
        }
    }

}
