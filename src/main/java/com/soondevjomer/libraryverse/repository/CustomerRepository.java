package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.Customer;
import com.soondevjomer.libraryverse.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser(User user);

    @Query("SELECT DISTINCT c FROM Customer c " +
            "JOIN Order o ON o.customer = c " +
            "JOIN StoreOrder so ON so.order = o " +
            "WHERE so.library.owner.id = :ownerId")
    Page<Customer> findCustomersByLibraryOwner(@Param("ownerId") Long ownerId, Pageable pageable);
}
