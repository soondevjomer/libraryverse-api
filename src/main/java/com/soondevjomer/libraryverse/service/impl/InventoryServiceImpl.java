package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.exception.InsufficientStockException;
import com.soondevjomer.libraryverse.model.Book;
import com.soondevjomer.libraryverse.model.Inventory;
import com.soondevjomer.libraryverse.model.User;
import com.soondevjomer.libraryverse.repository.BookRepository;
import com.soondevjomer.libraryverse.repository.InventoryRepository;
import com.soondevjomer.libraryverse.repository.UserRepository;
import com.soondevjomer.libraryverse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    public void reservedStock(Book book, Integer quantity) {
        // CHECK INVENTORY IF THE BOOK PROVIDED EXISTS
        Inventory inventory = inventoryRepository.findByBook(book)
                .orElseThrow(() -> new NoSuchElementException("Inventory not found"));
        // CHECK NO OF STOCK IF LESS THAN USER REQUEST THEN THROW ERROR
        if (inventory.getAvailableStock() < quantity) {
            throw new InsufficientStockException(
                    "Not enough stock for book: " + book.getBookDetail().getTitle() +
                            ". Available: " + inventory.getAvailableStock() +
                            ", requested: " + quantity
            );
        }
        // PROCEED TO RESERVED STOCK
        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        inventory.setReservedStock(inventory.getReservedStock() + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void shipStock(Book book, Integer quantity) {
        // CHECK INVENTORY IF THE BOOK PROVIDED EXISTS
        Inventory inventory = inventoryRepository.findByBook(book)
                .orElseThrow(() -> new NoSuchElementException("Inventory not found"));
        // CHECK IF RESERVED STOCK IS LESS THAN QUANTITY TO KEEP IT SYNC
        if (inventory.getReservedStock() < quantity) throw new InsufficientStockException("\"Not enough reserved stock to ship for book: " + book.getId());
        // PROCEED TO SHIPPED USER REQUEST
        inventory.setReservedStock(inventory.getReservedStock() - quantity);
        inventory.setShipped(inventory.getShipped() + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void deliverStock(Book book, Integer quantity) {
        Inventory inventory = inventoryRepository.findByBook(book)
                .orElseThrow(() -> new NoSuchElementException("Inventory not found"));
        if (inventory.getShipped() < quantity) throw new InsufficientStockException("Cannot deliver more than shipped for book: " + book.getId());
        inventory.setShipped(inventory.getShipped()-quantity);
        inventory.setDelivered(inventory.getDelivered()+quantity);
    }

    @Override
    public void releaseReserveStock(Book book, Integer quantity) {
        Inventory inventory = inventoryRepository.findByBook(book)
                .orElseThrow(() -> new RuntimeException("Inventory not found for book: " + book.getId()));

        if (inventory.getReservedStock() < quantity) {
            throw new InsufficientStockException("Cannot release more than reserved for book: " + book.getId());
        }

        inventory.setReservedStock(inventory.getReservedStock() - quantity);
        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
        inventoryRepository.save(inventory);
    }

    @Override
    public void updateStock(Book book, Integer quantity) {
        Inventory inventory = inventoryRepository.findByBook(book)
                .orElseGet(() ->
                    inventoryRepository.save(
                        Inventory.builder()
                            .book(book)
                            .reservedStock(0)
                            .shipped(0)
                            .delivered(0)
                            .build()
                    ));

        inventory.setAvailableStock(quantity<0 ? 0 : quantity);
        inventoryRepository.save(inventory);
    }
}
