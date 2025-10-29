package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.BookDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookDetailRepository extends JpaRepository<BookDetail, Long> {
}
