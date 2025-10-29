package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.service.StoreOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("storeOrders")
@RequiredArgsConstructor
public class StoreOrderController {

    private final StoreOrderService storeOrderService;

    @GetMapping()
    public ResponseEntity<?> getSalesByLibrarian(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder
    ) {

        return ResponseEntity.ok(storeOrderService.getSalesByLibrarianByPage(page, size, sortField, sortOrder));
    }

    @GetMapping("stat/customer")
    public ResponseEntity<?> getCustomerStatForLibrary() {

        return ResponseEntity.ok(storeOrderService.getCustomerStatForLibrary());
    }

    @GetMapping("stat/sales")
    public ResponseEntity<?> getSaleStatForLibrary() {
        return ResponseEntity.ok(storeOrderService.getSaleStatForLibrary());
    }

    @GetMapping("stat/orders")
    public ResponseEntity<?> getOrderStatForLibrary() {
        return ResponseEntity.ok(storeOrderService.getOrderStatForLibrary());
    }
}
