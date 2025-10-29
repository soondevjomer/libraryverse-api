package com.soondevjomer.libraryverse.mapper;

import com.soondevjomer.libraryverse.constant.OrderStatus;
import com.soondevjomer.libraryverse.constant.PaymentMethod;
import com.soondevjomer.libraryverse.constant.PaymentStatus;
import com.soondevjomer.libraryverse.dto.*;
import com.soondevjomer.libraryverse.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    public Order toOrder(OrderRequestDto dto, Customer customer) {
        PaymentMethod pm = PaymentMethod.valueOf(dto.getPaymentMethod());
        PaymentStatus ps = pm == PaymentMethod.COD ? PaymentStatus.PENDING : PaymentStatus.PAID;

        return Order.builder()
                .customer(customer)
                .paymentMethod(pm)
                .paymentStatus(ps)
                .totalAmount(BigDecimal.ZERO)
                .storeOrders(new ArrayList<>())
                .build();
    }

    public StoreOrder toStoreOrder(Order order, Library library) {
        StoreOrder so = StoreOrder.builder()
                .library(library)
                .order(order)
                .orderStatus(OrderStatus.PENDING)
                .subtotal(BigDecimal.ZERO)
                .orderItems(new ArrayList<>())
                .build();
        order.getStoreOrders().add(so);

        return so;
    }

    public OrderItem toOrderItem(StoreOrder storeOrder, Book book, OrderItemDto dto) {
        OrderItem oi = OrderItem.builder()
                .storeOrder(storeOrder)
                .book(book)
                .quantity(dto.getQuantity())
                .boughtAtPrice(dto.getPrice())
                .build();

        storeOrder.getOrderItems().add(oi);

        return oi;
    }

    // Response mappers

    public OrderResponseDto toOrderResponse(Order order) {
        List<StoreOrderDto> storeOrderDtos = order.getStoreOrders().stream()
                .map(this::toStoreOrderDto)
                .toList();

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .paymentMethod(order.getPaymentMethod().name())
                .totalAmount(order.getTotalAmount())
                .storeOrderDtos(storeOrderDtos)
                .build();
    }

    public StoreOrderDto toStoreOrderDto(StoreOrder so) {
        List<OrderItemDto> items = so.getOrderItems().stream()
                .map(this::toOrderItemDto)
                .toList();

        return StoreOrderDto.builder()
                .id(so.getId())
                .orderItemDtos(items)
                .subtotal(so.getSubtotal())
                .orderStatus(so.getOrderStatus().name())
                .build();
    }

    public OrderItemDto toOrderItemDto(OrderItem oi) {
        return OrderItemDto.builder()
                .bookId(oi.getBook().getId())
                .bookName(oi.getBook().getBookDetail().getTitle())
                .quantity(oi.getQuantity())
                .price(oi.getBoughtAtPrice())
                .build();
    }
}
