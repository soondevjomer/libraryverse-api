package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.dto.OrderRequestDto;
import com.soondevjomer.libraryverse.repository.OrderRepository;
import com.soondevjomer.libraryverse.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        return new ResponseEntity<>(orderService.createOrder(orderRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/cancel/{storeOrderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long storeOrderId) {
        orderService.cancelOrder(storeOrderId);
        return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
    }

    @PostMapping("/markAsShipped/{storeOrderId}")
    public ResponseEntity<?> markAsShipped(@PathVariable Long storeOrderId) {
        orderService.markAsShipped(storeOrderId);
        return ResponseEntity.ok(Map.of("message", "Order shipped successfully"));
    }

    @PostMapping("/markAsDelivered/{storeOrderId}")
    public ResponseEntity<?> markAsDelivered(@PathVariable Long storeOrderId) {
        orderService.markAsDelivered(storeOrderId);
        return ResponseEntity.ok(Map.of("message", "Order delivered successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getStoreOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String status
    ) {
        log.info("order status by: {}", status);
        return ResponseEntity.ok(orderService.getStoreOrderByPage(
                page, size, sortField, sortOrder, status
        ));
    }
}
