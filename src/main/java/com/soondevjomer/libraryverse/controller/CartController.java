package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    @PostMapping("/{bookId}")
        public ResponseEntity<?> addToCart(@PathVariable Long bookId) {

        boolean status = cartService.addToCart(bookId);
    
        return new ResponseEntity<>(status, status ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST);
    }

    @GetMapping
    public ResponseEntity<?> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<?> deleteCart(@PathVariable Long cartId) {
        cartService.removeCart(cartId);
        return ResponseEntity.ok(Map.of("message", "Cart deleted successfully"));
    }
    
}
