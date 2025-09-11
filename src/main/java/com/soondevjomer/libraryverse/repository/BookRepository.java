package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
