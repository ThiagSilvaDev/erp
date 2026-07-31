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
    private final GetCustomerByIdUseCase getCustomerByIdUseCase;
    private final ListCustomersUseCase listCustomersUseCase;

    CustomerController(
            CreateCustomerUseCase createCustomerUseCase,
            GetCustomerByIdUseCase getCustomerByIdUseCase,
            ListCustomersUseCase listCustomersUseCase) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.getCustomerByIdUseCase = getCustomerByIdUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
    }

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(@Valid CreateCustomerRequest request) {
        CustomerResponse response = createCustomerUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public CustomerResponse getCustomerById(UUID id) {
        return getCustomerByIdUseCase.execute(id);
    }

    @Override
    public Page<CustomerResponse> listCustomers(CustomerSearchCriteria criteria, Pageable pageable) {
        return listCustomersUseCase.execute(criteria, pageable);
    }
}
