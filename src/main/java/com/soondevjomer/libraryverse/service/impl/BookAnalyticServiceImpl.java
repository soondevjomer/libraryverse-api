package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.model.Book;
import com.soondevjomer.libraryverse.model.BookAnalytic;
import com.soondevjomer.libraryverse.model.Inventory;
import com.soondevjomer.libraryverse.repository.BookAnalyticRepository;
import com.soondevjomer.libraryverse.repository.BookRepository;
import com.soondevjomer.libraryverse.repository.InventoryRepository;
import com.soondevjomer.libraryverse.service.BookAnalyticService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookAnalyticServiceImpl implements BookAnalyticService {
    private final BookAnalyticRepository bookAnalyticRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final double VIEW_WEIGHT = 0.2;
    private final double SALE_WEIGHT = 0.8;

    @Override
    public void incrementViewCount(Long bookId) {
        log.info("increment view count of book id {}", bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));
        BookAnalytic bookAnalytic = bookAnalyticRepository.findByBookId(bookId)
                .orElseGet(
                        () -> BookAnalytic.builder().book(book).build()
                );
        long previousViewCount = bookAnalytic.getViewCount();
        bookAnalytic.setViewCount(bookAnalytic.getViewCount() + 1);
        BookAnalytic savedBookAnalytic = bookAnalyticRepository.save(bookAnalytic);
        log.info("view count from {} to {}", previousViewCount, savedBookAnalytic.getViewCount());
    }

    @Override
    public double getPopularityScore(Long bookId) {
        log.info("get the popularity score of book {}", bookId);
        BookAnalytic bookAnalytic = bookAnalyticRepository.findByBookId(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found"));

        long views = bookAnalytic.getViewCount();
        long sales = getSalesCount(bookId);

        log.info("book id of {} have {} views and {} sales", bookId, views, sales);

        return (views * VIEW_WEIGHT) + (sales * SALE_WEIGHT);
    }

    private Integer getSalesCount(Long bookId) {
        return inventoryRepository.findByBookId(bookId)
                .map(Inventory::getDelivered)
                .orElseThrow(() -> new NoSuchElementException("inventory not found with book id of {}" + bookId));
    }
}
