package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.dto.CustomerDto;
import com.soondevjomer.libraryverse.dto.PageModel;
import com.soondevjomer.libraryverse.model.Customer;
import com.soondevjomer.libraryverse.model.User;
import com.soondevjomer.libraryverse.repository.CustomerRepository;
import com.soondevjomer.libraryverse.repository.UserRepository;
import com.soondevjomer.libraryverse.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public PageModel<List<CustomerDto>> findCustomerByPage(Integer page, Integer size, String sortField, String sortOrder) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        List<String> allowedSortFields = List.of("id", "address", "contactNumber", "user.name");
        if (!allowedSortFields.contains(sortField)) {
            sortField = "id";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<Customer> customerPage = customerRepository.findCustomersByLibraryOwner(currentUser.getId(), pageable);

        List<CustomerDto> customerDtos = customerPage.stream()
                .map(customer ->
                    CustomerDto.builder()
                            .id(customer.getId())
                            .address(customer.getAddress())
                            .contactNumber(customer.getContactNumber())
                            .name(customer.getUser().getName())
                            .email(customer.getUser().getEmail())
                            .image(customer.getUser().getImage())
                            .build())
                .toList();

        return new PageModel<>(
                customerDtos,
                customerPage.getNumber(),
                customerPage.getSize(),
                customerPage.getTotalElements(),
                customerPage.getTotalPages()
        );
    }



}
