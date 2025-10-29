package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.CustomerDto;
import com.soondevjomer.libraryverse.dto.PageModel;

import java.util.List;

public interface CustomerService {

    PageModel<List<CustomerDto>> findCustomerByPage(Integer page, Integer size, String sortField, String sortOrder);
}
