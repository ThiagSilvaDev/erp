package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.customer.internal.dto.*;
import com.thiagsilvadev.erpapi.customer.internal.usecase.*;

@RestController
class CustomerController implements CustomerApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final CustomerQueryUseCase customerQueryUseCase;

    CustomerController(CreateCustomerUseCase createCustomerUseCase, CustomerQueryUseCase customerQueryUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.customerQueryUseCase = customerQueryUseCase;
    }

    @Override
    public ResponseEntity<CustomerResponse> create(@Valid CreateCustomerRequest request) {
        CustomerResponse response = createCustomerUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public CustomerResponse getById(UUID id) {
        return customerQueryUseCase.getById(id);
    }

    @Override
    public Page<CustomerResponse> list(CustomerSearchCriteria criteria, Pageable pageable) {
        return customerQueryUseCase.list(criteria, pageable);
    }
}
