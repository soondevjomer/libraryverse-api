package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.BookAnalytic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookAnalyticRepository extends JpaRepository<BookAnalytic, Long> {
    Optional<BookAnalytic> findByBookId(Long bookId);
}
