package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("uploads")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageService imageService;

    @PostMapping("/book-cover")
    public ResponseEntity<?> uploadBookCover(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bookTitle") String bookTitle,
            @RequestParam("libraryId") Long libraryId
    ) {
    log.info("uploading book cover with name of {}...", file.getName());
    return ResponseEntity.ok(imageService.uploadBookCover(file,bookTitle, libraryId));
    }

    @PostMapping("/library-cover")
    public ResponseEntity<?> uploadLibraryCover(
            @RequestParam("file") MultipartFile file,
            @RequestParam("libraryName") String libraryName,
            @RequestParam("libraryId") Long libraryId
    ) {
        log.info("uploading library cover with name of {}...", file.getName());
        return ResponseEntity.ok(imageService.uploadLibraryCover(file,libraryName, libraryId));
    }
}
