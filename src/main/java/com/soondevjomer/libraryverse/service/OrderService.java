package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.dto.OrderRequestDto;
import com.soondevjomer.libraryverse.dto.OrderResponseDto;
import com.soondevjomer.libraryverse.dto.PageModel;
import com.soondevjomer.libraryverse.dto.StoreOrderDto;
import com.soondevjomer.libraryverse.model.Book;
import com.soondevjomer.libraryverse.model.OrderItem;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);

    void markAsShipped(Long storeOrderId);

    void cancelOrder(Long storeOrderId);

    void markAsDelivered(Long storeOrderId);

    PageModel<List<StoreOrderDto>> getStoreOrderByPage(
            Integer page,
            Integer size,
            String sortField,
            String sortOrder,
            String status
    );



}