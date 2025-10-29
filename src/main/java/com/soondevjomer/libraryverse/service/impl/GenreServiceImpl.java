package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.model.Genre;
import com.soondevjomer.libraryverse.repository.GenreRepository;
import com.soondevjomer.libraryverse.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @Override
    public List<String> getGenres() {

        return genreRepository.findAll().stream().map(Genre::getName).toList();
    }
}
