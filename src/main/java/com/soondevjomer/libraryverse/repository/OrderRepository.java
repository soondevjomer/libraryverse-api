package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.Customer;
import com.soondevjomer.libraryverse.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o.customer FROM Order o")
    List<Customer> findAllCustomersWhoPlacedOrders();
}
