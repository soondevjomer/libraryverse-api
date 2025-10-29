package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
