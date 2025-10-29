package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.dto.*;
import com.soondevjomer.libraryverse.model.*;
import com.soondevjomer.libraryverse.repository.LibraryRepository;
import com.soondevjomer.libraryverse.repository.StoreOrderRepository;
import com.soondevjomer.libraryverse.repository.UserRepository;
import com.soondevjomer.libraryverse.service.StoreOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StoreOrderServiceImpl implements StoreOrderService {

    private final StoreOrderRepository storeOrderRepository;
    private final LibraryRepository libraryRepository;

    @Override
    public List<LibrarianSaleDto> getSalesByLibrarian() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);

        if (optionalLibrary.isEmpty()) {
            return Collections.emptyList();
        }

        Library library = optionalLibrary.get();

        List<StoreOrder> orders = storeOrderRepository.findCompletedSalesByLibraryId(library.getId());

        return orders.stream().map(so -> {
            LibrarianSaleDto librarianSaleDto = new LibrarianSaleDto();
            librarianSaleDto.setId(so.getId());
            librarianSaleDto.setSubtotal(so.getSubtotal());
            librarianSaleDto.setOrderStatus(so.getOrderStatus());
            librarianSaleDto.setOrderDate(so.getOrder().getOrderDate()); // assuming your Order has createdAt

            librarianSaleDto.setCustomerName(
                    so.getOrder().getCustomer().getUser().getName()
            );

            librarianSaleDto.setItems(
                    so.getOrderItems().stream().map(oi ->
                            SaleItemDto.builder()
                                    .bookTitle(oi.getBook().getBookDetail().getTitle())
                                    .quantity(oi.getQuantity())
                                    .pricePerItem(oi.getBoughtAtPrice())
                                    .total(oi.getBoughtAtPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                                    .build()
                    ).toList()
            );
            return librarianSaleDto;
        }).toList();
    }

    @Override
    public CustomerCountAndTopDto getCustomerStatForLibrary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);

        if (optionalLibrary.isEmpty()) {
            // Return default empty stats if the library doesn't exist yet
            CustomerCountAndTopDto emptyDto = new CustomerCountAndTopDto();
            emptyDto.setTotalCustomer(0);
            emptyDto.setTotalSpent(BigDecimal.ZERO);
            emptyDto.setTotalOrders(0);
            emptyDto.setTopCustomer(new CustomerDto()); // safe empty object
            return emptyDto;
        }

        Library library = optionalLibrary.get();

        List<StoreOrder> deliveredOrders = storeOrderRepository.findByLibraryIdAndOrderStatus(
                library.getId(), OrderStatus.DELIVERED);

        if (deliveredOrders == null || deliveredOrders.isEmpty()) {
            // If there are no delivered orders, return empty stats
            CustomerCountAndTopDto emptyDto = new CustomerCountAndTopDto();
            emptyDto.setTotalCustomer(0);
            emptyDto.setTotalSpent(BigDecimal.ZERO);
            emptyDto.setTotalOrders(0);
            emptyDto.setTopCustomer(new CustomerDto());
            return emptyDto;
        }

        Map<Customer, List<StoreOrder>> customerOrders = deliveredOrders.stream()
                .filter(so -> so.getOrder() != null && so.getOrder().getCustomer() != null)
                .collect(Collectors.groupingBy(so -> so.getOrder().getCustomer()));

        int totalCustomers = customerOrders.size();

        Customer topCustomer = null;
        BigDecimal topSpent = BigDecimal.ZERO;
        int topOrders = 0;

        for (Map.Entry<Customer, List<StoreOrder>> entry : customerOrders.entrySet()) {
            BigDecimal totalSpent = entry.getValue().stream()
                    .map(StoreOrder::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalSpent.compareTo(topSpent) > 0) {
                topSpent = totalSpent;
                topOrders = entry.getValue().size();
                topCustomer = entry.getKey();
            }
        }

        CustomerCountAndTopDto dto = new CustomerCountAndTopDto();
        dto.setTotalCustomer(totalCustomers);
        dto.setTotalSpent(topSpent);
        dto.setTotalOrders(topOrders);

        if (topCustomer != null) {
            CustomerDto customerDto = new CustomerDto();
            customerDto.setId(topCustomer.getId());
            customerDto.setName(topCustomer.getUser().getName());
            customerDto.setAddress(topCustomer.getAddress());
            customerDto.setContactNumber(topCustomer.getContactNumber());
            customerDto.setEmail(topCustomer.getUser().getEmail());
            dto.setTopCustomer(customerDto);
        } else {
            dto.setTopCustomer(new CustomerDto()); // safe empty object
        }

        return dto;
    }

    @Override
    public SaleStatDto getSaleStatForLibrary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // FIND LIBRARY SAFELY
        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);
        if (optionalLibrary.isEmpty()) {
            // RETURN EMPTY/DEFAULT STATS IF LIBRARY DOESN'T EXIST YET
            SaleStatDto emptyDto = new SaleStatDto();
            emptyDto.setTotalRevenue(BigDecimal.ZERO);
            emptyDto.setTotalPurchases(0);
            emptyDto.setTopBook(null);
            emptyDto.setTopAuthor(null);
            emptyDto.setTopGenre(null);
            return emptyDto;
        }

        Library library = optionalLibrary.get();

        // GET ALL DELIVERED STORE ORDERS SAFELY
        List<StoreOrder> deliveredOrders = library.getStoreOrders() == null ?
                Collections.emptyList() :
                library.getStoreOrders().stream()
                        .filter(so -> so.getOrderStatus() == OrderStatus.DELIVERED)
                        .toList();

        if (deliveredOrders.isEmpty()) {
            SaleStatDto emptyDto = new SaleStatDto();
            emptyDto.setTotalRevenue(BigDecimal.ZERO);
            emptyDto.setTotalPurchases(0);
            emptyDto.setTopBook(null);
            emptyDto.setTopAuthor(null);
            emptyDto.setTopGenre(null);
            return emptyDto;
        }

        // TOTAL REVENUE
        BigDecimal totalRevenue = deliveredOrders.stream()
                .map(so -> so.getSubtotal() != null ? so.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // TOTAL PURCHASES
        int totalPurchases = deliveredOrders.stream()
                .flatMap(so -> so.getOrderItems() != null ? so.getOrderItems().stream() : Stream.empty())
                .mapToInt(OrderItem::getQuantity) // primitive, safe
                .sum();

        // TOP BOOK
        Map<String, Integer> bookSales = deliveredOrders.stream()
                .flatMap(so -> so.getOrderItems() != null ? so.getOrderItems().stream() : Stream.empty())
                .filter(oi -> oi.getBook() != null && oi.getBook().getBookDetail() != null)
                .collect(Collectors.groupingBy(
                        oi -> oi.getBook().getBookDetail().getTitle(),
                        Collectors.summingInt(OrderItem::getQuantity)
                ));
        String topBook = bookSales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // TOP AUTHOR
        Map<String, Integer> authorSales = deliveredOrders.stream()
                .flatMap(so -> so.getOrderItems() != null ? so.getOrderItems().stream() : Stream.empty())
                .filter(oi -> oi.getBook() != null && oi.getBook().getBookDetail() != null)
                .flatMap(oi -> oi.getBook().getBookDetail().getAuthors() != null ?
                        oi.getBook().getBookDetail().getAuthors().stream()
                                .filter(Objects::nonNull)
                                .map(author -> Map.entry(author.getName(), oi.getQuantity()))
                        : Stream.empty())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));
        String topAuthor = authorSales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // TOP GENRE
        Map<String, Integer> genreSales = deliveredOrders.stream()
                .flatMap(so -> so.getOrderItems() != null ? so.getOrderItems().stream() : Stream.empty())
                .filter(oi -> oi.getBook() != null && oi.getBook().getBookDetail() != null)
                .flatMap(oi -> oi.getBook().getBookDetail().getGenres() != null ?
                        oi.getBook().getBookDetail().getGenres().stream()
                                .filter(Objects::nonNull)
                                .map(genre -> Map.entry(genre.getName(), oi.getQuantity()))
                        : Stream.empty())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingInt(Map.Entry::getValue)
                ));
        String topGenre = genreSales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        return SaleStatDto.builder()
                .totalRevenue(totalRevenue)
                .totalPurchases(totalPurchases)
                .topBook(topBook)
                .topAuthor(topAuthor)
                .topGenre(topGenre)
                .build();
    }

    @Override
    public OrderStatDto getOrderStatForLibrary() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);

        if (optionalLibrary.isEmpty()) {
            // RETURN SAFE DEFAULTS IF LIBRARY DOESN'T EXIST
            return OrderStatDto.builder()
                    .totalDelivered(0)
                    .totalPending(0)
                    .totalShipped(0)
                    .build();
        }

        Library library = optionalLibrary.get();
        List<StoreOrder> storeOrders = storeOrderRepository.findAllByLibraryId(library.getId());

        int delivered = (int) storeOrders.stream()
                .filter(so -> so.getOrderStatus() == OrderStatus.DELIVERED)
                .count();

        int pending = (int) storeOrders.stream()
                .filter(so -> so.getOrderStatus() == OrderStatus.PENDING)
                .count();

        int shipped = (int) storeOrders.stream()
                .filter(so -> so.getOrderStatus() == OrderStatus.SHIPPED)
                .count();

        return OrderStatDto.builder()
                .totalDelivered(delivered)
                .totalPending(pending)
                .totalShipped(shipped)
                .build();
    }

    @Override
    public PageModel<List<LibrarianSaleDto>> getSalesByLibrarianByPage(
        Integer page, Integer size, String sortField, String sortOrder) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Library> optionalLibrary = libraryRepository.findByOwnerUsername(username);

        // Return empty safe result if librarian has no library yet
        if (optionalLibrary.isEmpty()) {
            return new PageModel<>(Collections.emptyList(), page, size, 0, 0);
        }

        Library library = optionalLibrary.get();

        // Fetch all completed sales first
        List<StoreOrder> orders = storeOrderRepository.findCompletedSalesByLibraryId(library.getId());

        if (orders.isEmpty()) {
            return new PageModel<>(Collections.emptyList(), 0, size, 0, 0);
        }

        // Sorting
        Comparator<StoreOrder> comparator = getStoreOrderComparator(sortField, sortOrder);

        orders.sort(comparator);

        // Pagination
        int start = Math.min(page * size, orders.size());
        int end = Math.min(start + size, orders.size());
        List<StoreOrder> paginatedOrders = orders.subList(start, end);

        // CREAD
        List<LibrarianSaleDto> librarianSaleDtos = paginatedOrders.stream().map(so -> {
            LibrarianSaleDto dto = new LibrarianSaleDto();
            dto.setId(so.getId());
            dto.setSubtotal(so.getSubtotal());
            dto.setOrderStatus(so.getOrderStatus());
            dto.setOrderDate(so.getOrder().getOrderDate());
            dto.setCustomerName(so.getOrder().getCustomer().getUser().getName());

            List<SaleItemDto> items = so.getOrderItems().stream().map(oi ->
                    SaleItemDto.builder()
                            .bookTitle(oi.getBook().getBookDetail().getTitle())
                            .quantity(oi.getQuantity())
                            .pricePerItem(oi.getBoughtAtPrice())
                            .total(oi.getBoughtAtPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                            .build()
            ).toList();

            dto.setItems(items);
            return dto;
        }).toList();

        // WRAPPING IN PAGE MODEL
        int totalElements = orders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PageModel<>(
                librarianSaleDtos,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    private static Comparator<StoreOrder> getStoreOrderComparator(String sortField, String sortOrder) {
        Comparator<StoreOrder> comparator;
        switch (sortField == null ? "orderDate" : sortField) {
            case "customerName" ->
                    comparator = Comparator.comparing(so -> so.getOrder().getCustomer().getUser().getName(), String.CASE_INSENSITIVE_ORDER);
            case "subtotal" ->
                    comparator = Comparator.comparing(StoreOrder::getSubtotal);
            case "orderStatus" ->
                    comparator = Comparator.comparing(StoreOrder::getOrderStatus);
            default ->
                    comparator = Comparator.comparing(so -> so.getOrder().getOrderDate());
        }

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }


}
