package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.constant.PaymentStatus;
import com.soondevjomer.libraryverse.dto.PopularityDto;
import com.soondevjomer.libraryverse.model.*;
import com.soondevjomer.libraryverse.repository.BookRepository;
import com.soondevjomer.libraryverse.repository.LibraryRepository;
import com.soondevjomer.libraryverse.service.PopularityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularityServiceImpl implements PopularityService {

    private final BookRepository bookRepository;
    private final LibraryRepository libraryRepository;

    /**
     * Calculate the popularity score for a single book.
     * Formula: (views * 0.1) + (delivered sales * 0.9)
     */
    @Override
    @Transactional
    public PopularityDto calcBookPopulariyScore(Long bookId) {
        log.info("Calculating book popularity score for ID {}", bookId);

        final double VIEWS_WEIGHT = 0.1;
        final double SALES_WEIGHT = 0.9;

        Book book = bookRepository.findByIdWithRelations(bookId)
                .orElseThrow(() -> new NoSuchElementException("Book not found with ID: " + bookId));

        double bookViews = Optional.ofNullable(book.getViewCount()).orElse(0L).doubleValue();
        double deliveredSales = Optional.ofNullable(book.getInventory())
                .map(Inventory::getDelivered)
                .map(Integer::doubleValue)
                .orElse(0.0);

        double popularityScore = (bookViews * VIEWS_WEIGHT) + (deliveredSales * SALES_WEIGHT);

        Double maxPopularity = bookRepository.findMaxPopularityScore();
        if (maxPopularity == null || maxPopularity == 0) {
            maxPopularity = 1.0;
        }

        double normalizedScore = (popularityScore / maxPopularity) * 5.0;
        double roundedRating = Math.round(normalizedScore * 10.0) / 10.0;

        log.info("Book {} → rawScore={}, normalizedScore={}, roundedRating={}",
                bookId, popularityScore, normalizedScore, roundedRating);

        return PopularityDto.builder()
                .popularityScore(popularityScore)
                .roundedRating(roundedRating)
                .build();
    }

    /**
     * Calculate the popularity score for a single library.
     * Formula: (views * 0.1) + (totalRevenue * 0.6) + (totalSales * 0.3)
     */
    @Override
    @Transactional
    public PopularityDto calcLibraryPopularityScore(Long libraryId) {
        log.info("Calculating library popularity score for ID {}", libraryId);

        final double VIEWS_WEIGHT = 0.1;
        final double REVENUE_WEIGHT = 0.6;
        final double SALES_WEIGHT = 0.3;

        Library library = libraryRepository.findByIdWithRelations(libraryId)
                .orElseThrow(() -> new NoSuchElementException("Library not found with ID: " + libraryId));

        double libraryViews = Optional.ofNullable(library.getViewCount()).orElse(0L).doubleValue();

        // Compute total revenue from delivered + paid store orders
        double totalRevenue = library.getStoreOrders() == null ? 0.0 :
                library.getStoreOrders().stream()
                        .filter(Objects::nonNull)
                        .filter(order ->
                                order.getPaymentStatus() == PaymentStatus.PAID &&
                                        order.getOrderStatus() == OrderStatus.DELIVERED)
                        .map(StoreOrder::getSubtotal)
                        .filter(Objects::nonNull)
                        .mapToDouble(BigDecimal::doubleValue)
                        .sum();

        // Compute total sales from delivered books
        double totalSalesCount = library.getBooks() == null ? 0.0 :
                library.getBooks().stream()
                        .map(Book::getInventory)
                        .filter(Objects::nonNull)
                        .map(Inventory::getDelivered)
                        .filter(Objects::nonNull)
                        .mapToDouble(Integer::doubleValue)
                        .sum();

        double popularityScore = (libraryViews * VIEWS_WEIGHT)
                + (totalRevenue * REVENUE_WEIGHT)
                + (totalSalesCount * SALES_WEIGHT);

        Double maxPopularityScore = libraryRepository.findMaxPopularityScore();
        if (maxPopularityScore == null || maxPopularityScore == 0) {
            maxPopularityScore = 1.0;
        }

        double normalizedScore = (popularityScore / maxPopularityScore) * 5.0;
        double roundedRating = Math.round(normalizedScore * 10.0) / 10.0;

        log.info("Library {} → rawScore={}, normalizedScore={}, roundedRating={}",
                libraryId, popularityScore, normalizedScore, roundedRating);

        return PopularityDto.builder()
                .popularityScore(popularityScore)
                .roundedRating(roundedRating)
                .build();
    }
}
