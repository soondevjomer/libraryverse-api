package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.Cart;
import com.soondevjomer.libraryverse.model.Customer;
import com.soondevjomer.libraryverse.model.Library;
import com.soondevjomer.libraryverse.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByCartByUsername(String username);
    List<Cart> findByCartById(Long userId);
    Optional<Cart> findByCartByIdAndCartedBookId(Long userId, Long bookId);
    List<Cart> findByCartBy(User user);
    List<Cart> findByCartByAndCartedBook_Library(User user, Library library);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    void deleteByCartByAndCartedBookIdIn(User user, List<Long> bookIds);

}
