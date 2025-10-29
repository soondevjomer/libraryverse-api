package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.dto.*;
import com.soondevjomer.libraryverse.model.*;
import com.soondevjomer.libraryverse.repository.*;
import com.soondevjomer.libraryverse.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public boolean addToCart(Long bookId) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));

            Cart cart = cartRepository.findByCartByIdAndCartedBookId(user.getId(), bookId)
                    .orElse(Cart.builder()
                            .cartBy(user)
                            .quantity(1)
                            .cartedBook(book)
                            .build());

            cartRepository.save(cart);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<MyCartDto> getMyCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        // fetch all carts for a user
        List<Cart> carts = cartRepository.findByCartById(user.getId());

        // group by library
        Map<Library, List<Cart>> cartsByLibrary = carts.stream()
                .collect(Collectors.groupingBy(cart -> cart.getCartedBook().getLibrary()));

        // build MyCartDto list
        return cartsByLibrary.entrySet().stream()
                .map(entry -> {
                    Library library = entry.getKey();

                    // map carts inside this library
                    List<CartedDto> cartedDtos = entry.getValue().stream()
                            .map(cart -> CartedDto.builder()
                                    .cartId(cart.getId())
                                    .bookId(cart.getCartedBook().getId())
                                    .bookName(cart.getCartedBook().getBookDetail().getTitle())
                                    .price(cart.getCartedBook().getBookDetail().getPrice())
                                    .quantity(cart.getQuantity())
                                    .maxQuantity(getTotalAvailableCopy(cart.getCartedBook()))
                                    .build()
                            )
                            .collect(Collectors.toList());

                    // return MyCartDto
                    MyCartDto myCartDto = new MyCartDto();
                    myCartDto.setLibraryId(library.getId());
                    myCartDto.setLibraryName(library.getName());
                    myCartDto.setCarts(cartedDtos);

                    return myCartDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void removeCart(Long cartId) {
        log.info("removing this cart with id of {}", cartId);
        cartRepository.deleteById(cartId);
    }

    private Integer getTotalAvailableCopy(Book book) {
        Inventory inventory = inventoryRepository.findByBook(book)
                .orElseThrow(() -> new NoSuchElementException("Inventory not found with book id " + book.getId()));

        return inventory.getAvailableStock();
    }
}
