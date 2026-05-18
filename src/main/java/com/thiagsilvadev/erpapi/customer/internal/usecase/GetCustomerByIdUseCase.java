package com.thiagsilvadev.erpapi.customer.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerResponse;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;

@Component
public class GetCustomerByIdUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    public GetCustomerByIdUseCase(CustomerRepository customerRepository, CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.customer.api.CustomerPermissions).READ)")
    public CustomerResponse execute(UUID id) {
        return mapper.toResponse(
                customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Customer.class, id)));
    }
}
