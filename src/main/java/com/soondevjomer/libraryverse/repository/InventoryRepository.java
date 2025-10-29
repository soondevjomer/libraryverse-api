package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.Book;
import com.soondevjomer.libraryverse.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByBook(Book book);

    Optional<Inventory> findByBookId(Long bookId);
}
