package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.constant.SortBy;
import com.soondevjomer.libraryverse.constant.SortDirection;
import com.soondevjomer.libraryverse.dto.*;
import com.soondevjomer.libraryverse.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/libraries")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping("/stats")
    public ResponseEntity<?> getLibraryStat() {
        return ResponseEntity.ok(libraryService.getLibraryStat());
    }

    @GetMapping
    public ResponseEntity<?> getLibraryByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String sortBy,
            @RequestParam(defaultValue = "") String sortDirection,
            @RequestParam(defaultValue = "0") int libraryId
    ) {
        return ResponseEntity.ok(libraryService.getLibraryByPage(
                FilterDto.builder()
                        .page(page)
                        .size(size)
                        .sortField(sortField)
                        .sortOrder(sortOrder)
                        .search(search)
                        .sortBy(SortBy.valueOf(sortBy))
                        .sortDirection(SortDirection.valueOf(sortDirection))
                        .libraryId(libraryId)
                        .build()
        ));
    }

    @GetMapping("/{libraryId}")
    public ResponseEntity<?> getLibraryById(@PathVariable Long libraryId) {
        return ResponseEntity.ok(libraryService.getLibraryById(libraryId));
    }

    @PutMapping(value = "/{libraryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateLibraryById(
            @PathVariable Long libraryId,
            @RequestPart(value = "library") LibraryRequestDto libraryRequestDto,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return ResponseEntity.ok(libraryService.updateLibraryById(libraryId, libraryRequestDto, file));
    }
}
