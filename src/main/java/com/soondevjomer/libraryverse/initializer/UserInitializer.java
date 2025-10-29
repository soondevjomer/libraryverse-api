package com.soondevjomer.libraryverse.initializer;

import com.soondevjomer.libraryverse.constant.Role;
import com.soondevjomer.libraryverse.model.*;
import com.soondevjomer.libraryverse.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInitializer {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final InventoryRepository inventoryRepository;
    private final BookDetailRepository bookDetailRepository;
    private final PasswordEncoder passwordEncoder;

    public void initDefaultLibrarian() {
        if (userRepository.count() > 0) {
            log.info("Users already exist — skipping librarian initialization");
            return;
        }

        log.info("Create default user");

//        log.info("Create default librarian");
//        User librarian = new User();
//        librarian.setName("Default Librarian");
//        librarian.setUsername("aa");
//        librarian.setEmail("aa@example.com");
//        librarian.setPassword(passwordEncoder.encode("aa"));
//        librarian.setRole(Role.LIBRARIAN);
//        userRepository.save(librarian);
//        log.info("Default librarian user created: username=aa, password=aa");
//
//        log.info("Create library for librarian");
//        Library library = new Library();
//        library.setName("AA Library");
//        library.setAddress("Default Address");
//        library.setContactNumber("000-000-0000");
//        library.setDescription("This is the default library for the initial librarian.");
//        library.setOwner(librarian);
//        library.setViewCount(0L);
//        libraryRepository.save(library);
//        log.info("✅ Default library created for librarian: {}", library.getName());
//
//        log.info("Create BookDetail");
//        BookDetail detail = new BookDetail();
//        detail.setTitle("Sample Book");
//        detail.setDescription("This is a sample book automatically created for the default library.");
//        detail.setPublishedYear(2025L);
//        detail.setPrice(BigDecimal.valueOf(9.99));
//        bookDetailRepository.save(detail);
//
//        log.info("Create Book linked to the library");
//        Book book = new Book();
//        book.setIsbn("ISBN-0000-AA");
//        book.setLibrary(library);
//        book.setBookDetail(detail);
//        book.setViewCount(0L);
//
//        log.info("Create Inventory");
//        Inventory inventory = new Inventory();
//        inventory.setAvailableStock(0);
//        inventory.setShipped(0);
//        inventory.setReservedStock(0);
//        inventory.setDelivered(0);
//        inventory.setBook(book);
//        book.setInventory(inventory);
//
//        bookRepository.save(book);
//        log.info("Sample book added to default library: {}", detail.getTitle());
    }
}
