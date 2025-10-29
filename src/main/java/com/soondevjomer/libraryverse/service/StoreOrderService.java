package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.*;

import java.util.List;

public interface StoreOrderService {

    List<LibrarianSaleDto> getSalesByLibrarian();

    CustomerCountAndTopDto getCustomerStatForLibrary();

    SaleStatDto getSaleStatForLibrary();

    OrderStatDto getOrderStatForLibrary();

    PageModel<List<LibrarianSaleDto>> getSalesByLibrarianByPage(Integer page, Integer size, String sortField, String sortOrder);
}
