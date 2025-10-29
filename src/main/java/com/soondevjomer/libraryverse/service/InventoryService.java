package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.model.Book;

public interface InventoryService {

    void reservedStock(Book book, Integer quantity);

    void shipStock(Book book, Integer quantity);

    void deliverStock(Book book, Integer quantity);

    void releaseReserveStock(Book book, Integer quantity);

    void updateStock(Book book, Integer quantity);
}
