package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.constant.PaymentStatus;
import com.soondevjomer.libraryverse.dto.PopularityDto;
import com.soondevjomer.libraryverse.model.*;
import com.soondevjomer.libraryverse.repository.BookRepository;
import com.soondevjomer.libraryverse.repository.InventoryRepository;
import com.soondevjomer.libraryverse.repository.LibraryRepository;
import com.soondevjomer.libraryverse.service.PopularityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularityServiceImpl implements PopularityService {
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final LibraryRepository libraryRepository;

    @Override
    public PopularityDto calcBookPopulariyScore(Long bookid) {
        log.info("Calculating book popularity score...");
        double VIEWS_WEIGHT = 0.2;
        double SALES_WEIGHT = 0.8;

        Book book = bookRepository.findById(bookid)
                .orElseThrow(()->new NoSuchElementException("Book not found"));
        if (book == null) return PopularityDto.builder().popularityScore(0).build();

        double bookViews = Optional.ofNullable(book.getViewCount()).orElse(0L);
        double salesCount = Optional.ofNullable(book.getInventory())
                .map(inv -> Optional.ofNullable(inv.getDelivered()).orElse(0))
                .map(Integer::doubleValue)
                .orElse(0.0);

        double popularityScore = (bookViews * VIEWS_WEIGHT) + (salesCount * SALES_WEIGHT);

        Double maxPopularity = bookRepository.findMaxPopularityScore();
        if (maxPopularity == null || maxPopularity == 0) {
            maxPopularity = 1.0;
        }

        double normalizedScore = (popularityScore / maxPopularity) * 5.0;
        double roundedRating = Math.round(normalizedScore * 10.0) / 10.0;

        log.info("Calculated popularity score for book {}: {}", bookid, popularityScore);
        return PopularityDto.builder()
                .popularityScore(popularityScore)
                .roundedRating(roundedRating)
                .build();
    }

    public PopularityDto calcLibraryPopularityScore(Long libraryId) {
        log.info("Calculating library popularity score...");

        double VIEWS_WEIGHT = 0.1;
        double REVENUE_WEIGHT = 0.6;
        double SALES_WEIGHT = 0.3;

        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new NoSuchElementException("Library not found with ID: " + libraryId));

        log.info("Getting view count for library...");
        double libraryViews = library.getViewCount() != null ? library.getViewCount() : 0.0;

        log.info("Calculating total revenue from store orders (PAID + DELIVERED)...");
        double totalRevenue = Optional.ofNullable(library.getStoreOrders())
                .orElse(Collections.emptyList())
                .stream()
                .filter(storeOrder ->
                        storeOrder.getPaymentStatus() == PaymentStatus.PAID &&
                                storeOrder.getOrderStatus() == OrderStatus.DELIVERED)
                .map(StoreOrder::getSubtotal)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();

        log.info("Calculating total sales count from delivered books...");
        double totalSalesCount = Optional.ofNullable(library.getBooks())
                .orElse(Collections.emptyList())
                .stream()
                .map(Book::getInventory)
                .filter(Objects::nonNull)
                .map(Inventory::getDelivered)
                .filter(Objects::nonNull)
                .mapToDouble(Integer::doubleValue)
                .sum();


        log.info("Applying popularity formula: (views * {}) + (revenue * {}) + (sales * {})",
                VIEWS_WEIGHT, REVENUE_WEIGHT, SALES_WEIGHT);

        double popularityScore = (libraryViews * VIEWS_WEIGHT)
                + (totalRevenue * REVENUE_WEIGHT)
                + (totalSalesCount * SALES_WEIGHT);

        BigDecimal roundedScore = BigDecimal.valueOf(popularityScore)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Calculated popularity score for library {}: {}", libraryId, popularityScore);

        return PopularityDto.builder()
                .popularityScore(roundedScore.doubleValue())
                .build();
    }
}
