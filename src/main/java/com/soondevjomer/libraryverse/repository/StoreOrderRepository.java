package com.soondevjomer.libraryverse.repository;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.model.Customer;
import com.soondevjomer.libraryverse.model.Library;
import com.soondevjomer.libraryverse.model.StoreOrder;
import com.soondevjomer.libraryverse.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {

    List<StoreOrder> findAllByLibraryId(Long librarianId);

    @Query("""
    SELECT so FROM StoreOrder so
    WHERE so.library.id = :libraryId
    AND so.orderStatus = 'DELIVERED'
    AND so.paymentStatus = 'PAID'
""")
    List<StoreOrder> findCompletedSalesByLibraryId(@Param("libraryId") Long libraryId);

    List<StoreOrder> findByLibraryIdAndOrderStatus(Long libraryId, OrderStatus orderStatus);

    Page<StoreOrder> findByLibrary(Library library, Pageable pageable);

    Page<StoreOrder> findByLibraryAndOrderStatus(Library library, OrderStatus status, Pageable pageable);

    Page<StoreOrder> findByOrder_Customer(Customer customer, Pageable pageable);
    Page<StoreOrder> findByOrder_CustomerAndOrderStatus(Customer customer, OrderStatus status, Pageable pageable);
    Page<StoreOrder> findByOrderStatus(OrderStatus status, Pageable pageable);

}
