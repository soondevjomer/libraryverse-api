package com.soondevjomer.libraryverse.mapper;

import com.soondevjomer.libraryverse.dto.LibraryDto;
import com.soondevjomer.libraryverse.dto.LibraryRequestDto;
import com.soondevjomer.libraryverse.dto.PopularityDto;
import com.soondevjomer.libraryverse.model.Library;
import com.soondevjomer.libraryverse.service.PopularityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryMapper {
    private final PopularityService popularityService;

    public LibraryDto toDto(Library library) {
        if (library == null) return null;

        PopularityDto libraryPopularDto = popularityService.calcLibraryPopularityScore(library.getId());

        return LibraryDto.builder()
                .id(library.getId())
                .name(library.getName())
                .address(library.getAddress())
                .contactNumber(library.getContactNumber())
                .ownerName(library.getOwner()!=null ? library.getOwner().getName() : "")
                .description(library.getDescription())
                .libraryCover(library.getLibraryCover())
                .libraryThumbnailCover(library.getLibraryThumbnailCover())
                .popularityScore(libraryPopularDto.getPopularityScore())
                .roundedRating(libraryPopularDto.getRoundedRating())
                .build();
    }

    public Library toEntity(LibraryDto libraryDto) {
        if (libraryDto == null) return null;
        return Library.builder()
                .name(libraryDto.getName())
                .address(libraryDto.getAddress())
                .contactNumber(libraryDto.getContactNumber())
                .description(libraryDto.getDescription())
                .libraryCover(libraryDto.getLibraryCover())
                .libraryThumbnailCover(libraryDto.getLibraryThumbnailCover())
                .build();
    }

    public Library mergeLibrary(Library library, LibraryRequestDto dto) {

        library.setName(dto.getName());
        library.setAddress(dto.getAddress());
        library.setContactNumber(dto.getContactNumber());
        library.setDescription(dto.getDescription());
        library.setLibraryCover(library.getLibraryCover());
        library.setLibraryThumbnailCover(library.getLibraryThumbnailCover());

        return library;
    }
}
