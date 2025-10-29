package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.CartDto;
import com.soondevjomer.libraryverse.dto.MyCartDto;
import com.soondevjomer.libraryverse.dto.PageModel;

import java.util.List;

public interface CartService {

    boolean addToCart(Long bookId);

    List<MyCartDto> getMyCart();

    void removeCart(Long cartId);
}
