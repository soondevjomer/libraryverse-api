package com.soondevjomer.libraryverse.initializer;

import com.soondevjomer.libraryverse.model.Genre;
import com.soondevjomer.libraryverse.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreInitializer {

    private final GenreRepository genreRepository;

    public void initGenres() {
        if (genreRepository.count()==0) {
            genreRepository.saveAll(
                    List.of(
                            Genre.builder().name("Comedy").build(),
                            Genre.builder().name("Romance").build(),
                            Genre.builder().name("Historical").build(),
                            Genre.builder().name("Fantasy").build(),
                            Genre.builder().name("Horror").build(),
                            Genre.builder().name("Mystery").build(),
                            Genre.builder().name("Thriller").build(),
                            Genre.builder().name("Fiction").build(),
                            Genre.builder().name("Non-Fiction").build()
                    )
            );
        }
    }
}
