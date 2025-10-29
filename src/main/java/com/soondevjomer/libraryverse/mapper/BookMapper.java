package com.soondevjomer.libraryverse.mapper;

import com.soondevjomer.libraryverse.dto.*;
import com.soondevjomer.libraryverse.model.*;
import com.soondevjomer.libraryverse.repository.*;
import com.soondevjomer.libraryverse.service.PopularityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class BookMapper {
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final InventoryRepository  inventoryRepository;
    private final TagRepository tagRepository;
    private final PopularityService popularityService;

    public BookDto toDto(Book book) {
        if (book == null) return null;

        BookDetail detail = book.getBookDetail();
        BookDetailDto detailDto = null;

        if (detail != null) {
            detailDto = BookDetailDto.builder()
                    .id(detail.getId())
                    .title(detail.getTitle())
                    .seriesTitle(detail.getSeriesTitle())
                    .description(detail.getDescription())
                    .bookCover(detail.getBookCover())
                    .price(detail.getPrice())
                    .publishedYear(detail.getPublishedYear())
                    .genres(detail.getGenres() == null ? List.of() :
                            detail.getGenres().stream().map(Genre::getName).toList())
                    .authors(detail.getAuthors() == null ? List.of() :
                            detail.getAuthors().stream().map(Author::getName).toList())
                    .publisher(detail.getPublisher() != null ? detail.getPublisher().getName() : null)
                    .build();
        }

        Inventory inventory = book.getInventory();
        InventoryDto inventoryDto = new InventoryDto();
        if (inventory!=null) {
            inventoryDto.setAvailableStock(inventory.getAvailableStock());
            inventoryDto.setDelivered(inventory.getDelivered());
            inventoryDto.setShipped(inventory.getShipped());
            inventoryDto.setReservedStock(inventory.getReservedStock());
            inventoryDto.setId(inventory.getId());
        }

        PopularityDto bookPopularityDto = popularityService.calcBookPopulariyScore(book.getId());

        return BookDto.builder()
                .id(book.getId())
                .libraryId(book.getLibrary().getId())
                .isbn(book.getIsbn())
                .bookDetail(detailDto)
                .inventory(inventoryDto)
                .createdDate(book.getCreatedDate())
                .popularityScore(bookPopularityDto.getPopularityScore())
                .roundedRating(bookPopularityDto.getRoundedRating())
                .build();
    }

    public Book toEntity(BookDto dto) {
        if (dto == null) return null;

        return Book.builder()
                .isbn(dto.getIsbn())
                .bookDetail(toBookDetail(dto.getBookDetail()))
                .build();
    }

    public BookDetail toBookDetail(BookDetailDto dto) {
        if (dto == null) return null;

        BookDetail detail = new BookDetail();
        detail.setTitle(dto.getTitle());
        detail.setSeriesTitle(dto.getSeriesTitle());
        detail.setDescription(dto.getDescription());
        detail.setBookCover(dto.getBookCover());
        detail.setPrice(dto.getPrice());
        detail.setPublishedYear(dto.getPublishedYear());

        if (dto.getAuthors() != null) {
            List<Author> authors = dto.getAuthors().stream()
                    .map(name -> authorRepository.findByName(name)
                            .orElseGet(() -> Author.builder().name(name).build()))
                    .collect(Collectors.toCollection(ArrayList::new));
            detail.setAuthors(authors);
        }

        if (dto.getGenres() != null) {
            List<Genre> genres = dto.getGenres().stream()
                    .map(name -> genreRepository.findByName(name)
                            .orElseGet(() -> Genre.builder().name(name).build()))
                    .collect(Collectors.toCollection(ArrayList::new));
            detail.setGenres(genres);
        }

        if (dto.getPublisher() != null) {
            Publisher publisher = publisherRepository.findByName(dto.getPublisher())
                    .orElseGet(() -> Publisher.builder().name(dto.getPublisher()).build());
            detail.setPublisher(publisher);
        }

        return detail;
    }

    public BookDetail mergeBookDetail(BookDetail existing, BookDetailDto dto) {
        log.info("Book existing is merging");
        if (dto == null) return existing;
        log.info("Dto is not null");

        if (existing == null) {
            log.info("Book existing is null");
            return toBookDetail(dto); // creation case
        }

        existing.setTitle(dto.getTitle());
        existing.setSeriesTitle(dto.getSeriesTitle());
        existing.setDescription(dto.getDescription());
        existing.setBookCover(dto.getBookCover());
        existing.setPrice(dto.getPrice());
        existing.setPublishedYear(dto.getPublishedYear());

        if (dto.getAuthors() != null) {
            log.info("DTO authors is not null");
            List<Author> authors = dto.getAuthors().stream()
                    .map(name -> authorRepository.findByName(name)
                            .orElseGet(() -> Author.builder().name(name).build()))
                    .collect(Collectors.toCollection(ArrayList::new));
            existing.setAuthors(authors);
        }

        if (dto.getGenres() != null) {
            log.info("DTO genres is not null");
            List<Genre> genres = dto.getGenres().stream()
                    .map(name -> genreRepository.findByName(name)
                            .orElseGet(() -> Genre.builder().name(name).build()))
                    .collect(Collectors.toCollection(ArrayList::new));
            existing.setGenres(genres);
        }

        if (dto.getPublisher() != null) {
            log.info("DTO publisher is not null");
            Publisher publisher = publisherRepository.findByName(dto.getPublisher())
                    .orElseGet(() -> Publisher.builder().name(dto.getPublisher()).build());
            existing.setPublisher(publisher);
        }

        log.info("returning edited existing book");
        return existing;
    }

}
