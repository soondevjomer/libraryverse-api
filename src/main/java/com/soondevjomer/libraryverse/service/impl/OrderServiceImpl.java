package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.constant.PaymentMethod;
import com.soondevjomer.libraryverse.constant.PaymentStatus;
import com.soondevjomer.libraryverse.constant.Role;
import com.soondevjomer.libraryverse.dto.*;
import com.soondevjomer.libraryverse.mapper.OrderMapper;
import com.soondevjomer.libraryverse.model.*;
import com.soondevjomer.libraryverse.repository.*;
import com.soondevjomer.libraryverse.service.InventoryService;
import com.soondevjomer.libraryverse.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final CartRepository cartRepository;
    private final LibraryRepository libraryRepository;
    private final OrderMapper orderMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        log.info("ORDER SERVICE START");
        log.info("ORDER SERVICE PAYMENT METHOD: {}", orderRequestDto.getPaymentMethod());
        User currentUser = getCurrentUser();

        log.info("get/create customer");
        Customer customer = customerRepository
                .findByUser(currentUser)
                .map(existingCustomer -> {
                    // update existing customer
                    existingCustomer.setAddress(orderRequestDto.getAddress());
                    existingCustomer.setContactNumber(orderRequestDto.getContactNumber());
                    return existingCustomer;
                })
                .orElseGet(() -> {
                    // create a new customer
                    return customerRepository.save(
                            Customer.builder()
                                    .user(currentUser)
                                    .address(orderRequestDto.getAddress())
                                    .contactNumber(orderRequestDto.getContactNumber())
                                    .build()
                    );
                });

        log.info("get/create customer id: {}", customer.getId());

        PaymentMethod pm = PaymentMethod.valueOf(orderRequestDto.getPaymentMethod());
        PaymentStatus ps = pm == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.PAID;
        log.info("payment method {} and status {}", pm, ps);

        // Build base order (not yet saved)
        Order order = orderMapper.toOrder(orderRequestDto, customer);
        log.info("check order is created by its customer id {}", order.getCustomer().getId());

        Map<Library, List<OrderItemDto>> itemsByLibrary = groupItemsByLibrary(orderRequestDto.getOrderItemDtos());
        log.info("group items by library isEmpty? {}", itemsByLibrary.isEmpty());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (var entry : itemsByLibrary.entrySet()) {
            log.info("looping itemsByLibrary entry set...");
            Library library = entry.getKey();
            List<OrderItemDto> itemDtos = entry.getValue();

            StoreOrder storeOrder = orderMapper.toStoreOrder(order, library);
            storeOrder.setPaymentStatus(ps);
            log.info("check store order created by its library id {}", storeOrder.getLibrary().getId());

            BigDecimal subtotal = BigDecimal.ZERO;

            for (OrderItemDto itemDto : itemDtos) {
                log.info("looping through item dtos...");
                Book book = bookRepository.findById(itemDto.getBookId())
                        .orElseThrow(() -> new NoSuchElementException("Book not found: " + itemDto.getBookId()));

                itemDto.setBookName(book.getBookDetail().getTitle());
                itemDto.setPrice(book.getBookDetail().getPrice());
                log.info("item dto bookname {} and price {} set", itemDto.getBookName(), itemDto.getPrice());

                log.info("reserve the stock");
                inventoryService.reservedStock(book, itemDto.getQuantity());

                OrderItem orderItem = orderMapper.toOrderItem(storeOrder, book, itemDto);
                log.info("check order item created by its bought at price {}", orderItem.getBoughtAtPrice());

                subtotal = subtotal.add(itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
                log.info("subtotal computed: {}", subtotal);
            }

            storeOrder.setSubtotal(subtotal);
            log.info("store order set its subtotal");

            totalAmount = totalAmount.add(subtotal);
            log.info("total amount computed: {}", totalAmount);
        }

        order.setTotalAmount(totalAmount);
        log.info("saving order now");
        Order saved = orderRepository.save(order);
        log.info("check order saved by id {}", saved.getId());

        List<Long> orderedBookIds = orderRequestDto.getOrderItemDtos().stream()
                .map(OrderItemDto::getBookId)
                .toList();
        log.info("ordered book count: {}", orderedBookIds.size());
        cartRepository.deleteByCartByAndCartedBookIdIn(currentUser, orderedBookIds);

        log.info("OrderResponse constructed");
        return orderMapper.toOrderResponse(saved);
    }


    @Override
    public void markAsShipped(Long storeOrderId) {
        log.info("MARKING AS SHIPPED THE STORE ID: {}", storeOrderId);
        User currentUser = getCurrentUser();
        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new NoSuchElementException("Store order not found"));

        // ensure the library owner is current user
        if (!storeOrder.getLibrary().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not owner of this library");
        }

        if (storeOrder.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot ship order in status: " + storeOrder.getOrderStatus());
        }

        // UPDATE INVENTORY
        for (OrderItem item : storeOrder.getOrderItems()) {
            inventoryService.shipStock(item.getBook(), item.getQuantity());
        }

        storeOrder.setOrderStatus(OrderStatus.SHIPPED);
        storeOrderRepository.save(storeOrder);
    }

    @Override
    public void cancelOrder(Long storeOrderId) {
        User currentUser = getCurrentUser();
        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new NoSuchElementException("Store order not found"));

        User libraryOwner = storeOrder.getLibrary().getOwner();
        User orderCustomer = storeOrder.getOrder()
                .getCustomer()
                .getUser();

        log.info("Allow if the current user is either the library owner or the customer who placed the order");
        boolean isLibraryOwner = libraryOwner != null && libraryOwner.getId().equals(currentUser.getId());
        boolean isOrderCustomer = orderCustomer != null && orderCustomer.getId().equals(currentUser.getId());

        if (!isLibraryOwner && !isOrderCustomer) {
            throw new AccessDeniedException("You are not authorized to cancel this order");
        }

        if (storeOrder.getOrderStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel shipped order");
        }

        for (OrderItem item : storeOrder.getOrderItems()) {
            inventoryService.releaseReserveStock(item.getBook(), item.getQuantity());
        }

        storeOrder.setOrderStatus(OrderStatus.CANCELLED);
        storeOrderRepository.save(storeOrder);
    }

    @Transactional
    @Override
    public void markAsDelivered(Long storeOrderId) {
        User currentUser = getCurrentUser();
        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new NoSuchElementException("Store order not found"));
        log.info("ensure the library owner is current user");
        if (!storeOrder.getLibrary().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not owner of this library");
        }

        if (!storeOrder.getOrderStatus().equals(OrderStatus.SHIPPED)) {
            throw new RuntimeException("Only shipped orders can be marked as delivered");
        }

        for (OrderItem item : storeOrder.getOrderItems()) {
            inventoryService.deliverStock(item.getBook(), item.getQuantity());
        }

        storeOrder.setOrderStatus(OrderStatus.DELIVERED);
        storeOrder.setPaymentStatus(PaymentStatus.PAID);
        StoreOrder savedStoreOrder = storeOrderRepository.save(storeOrder);
        updateOrderPaymentStatusToPaid(savedStoreOrder.getOrder().getId());
    }

    @Override
    public PageModel<List<StoreOrderDto>> getStoreOrderByPage(Integer page, Integer size, String sortField, String sortOrder, String status) {
        log.info("Getting store orders...");
        User currentUser = getCurrentUser();
        Role role = currentUser.getRole();

        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<StoreOrder> storeOrdersPage;

        if (role == Role.LIBRARIAN) {
            log.info("getting orders for librarian ...");
            Library library = libraryRepository.findByOwnerUsername(currentUser.getUsername())
                    .orElseThrow(() -> new NoSuchElementException("Library not found"));

            storeOrdersPage = getStoreOrdersForLibrary(library, status, pageable);

        } else if (role == Role.READER) {
            log.info("getting orders for customer ...");
            Optional<Customer> optionalCustomer = customerRepository.findByUser(currentUser);

            if (optionalCustomer.isEmpty()) {
                log.warn("No customer profile found for user '{}'. Returning empty order page.", currentUser.getUsername());
                return new PageModel<>(
                        List.of(),
                        page,
                        size,
                        0,
                        0
                );
            }

            Customer customer = optionalCustomer.get();
            storeOrdersPage = getStoreOrdersForCustomer(customer, status, pageable);

        }
        else {
            throw new IllegalStateException("Unsupported role: " + role);
        }

        List<StoreOrderDto> storeDtos = storeOrdersPage.stream()
                .map(orderMapper::toStoreOrderDto)
                .toList();

        return new PageModel<>(
                storeDtos,
                storeOrdersPage.getNumber(),
                storeOrdersPage.getSize(),
                storeOrdersPage.getTotalElements(),
                storeOrdersPage.getTotalPages()
        );
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    private Map<Library, List<OrderItemDto>> groupItemsByLibrary(List<OrderItemDto> items) {
        Map<Library, List<OrderItemDto>> map = new HashMap<>();
        for (OrderItemDto dto : items) {
            Book book = bookRepository.findById(dto.getBookId())
                    .orElseThrow(() -> new NoSuchElementException("Book not found: " + dto.getBookId()));
            map.computeIfAbsent(book.getLibrary(), k -> new ArrayList<>()).add(dto);
        }
        return map;
    }

    private void updateOrderPaymentStatusToPaid(Long orderId) {
        log.info("because a store order mark as delivered/paid so we need to update if an all of store orders of order is paid");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("No order found"));

        boolean isAllPaid = order.getStoreOrders().stream().allMatch(
                (storeOrder -> storeOrder.getPaymentStatus().equals(PaymentStatus.PAID))
        );

        if (isAllPaid) {
            log.info("all store order is paid so the order must be mark as paid");
            if (order.getPaymentStatus().equals(PaymentStatus.PAID)) {
                log.info("order is already mark as paid");
            } else if (order.getPaymentStatus().equals(PaymentStatus.PENDING)) {
                log.info("order is now mark as paid");
                order.setPaymentStatus(PaymentStatus.PAID);
                orderRepository.save(order);
            }
        }
    }

    private Page<StoreOrder> getStoreOrdersForLibrary(Library library, String status, Pageable pageable) {
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return storeOrderRepository.findByLibraryAndOrderStatus(library, orderStatus, pageable);
        }
        return storeOrderRepository.findByLibrary(library, pageable);
    }

    private Page<StoreOrder> getStoreOrdersForCustomer(Customer customer, String status, Pageable pageable) {
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            log.info("getting store order with order status");
            return storeOrderRepository.findByOrder_CustomerAndOrderStatus(customer, orderStatus, pageable);
        }
        log.info("getting store order without order status");
        return storeOrderRepository.findByOrder_Customer(customer, pageable);
    }

    private Page<StoreOrder> getStoreOrdersForAdmin(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return storeOrderRepository.findByOrderStatus(orderStatus, pageable);
        }
        return storeOrderRepository.findAll(pageable);
    }

}
