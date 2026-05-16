package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.api.*;
import com.thiagsilvadev.erpapi.customer.internal.dto.*;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerSpecification;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;
import com.thiagsilvadev.erpapi.customer.internal.usecase.CreateCustomerUseCase;

@RestController
class CustomerController implements CustomerApi, CustomerFacade {

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final CompanyFacade companyFacade;
    private final CustomerMapper mapper;
    private final CreateCustomerUseCase createCustomerUseCase;

    CustomerController(
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            CompanyFacade companyFacade,
            CustomerMapper mapper,
            CreateCustomerUseCase createCustomerUseCase) {
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.companyFacade = companyFacade;
        this.mapper = mapper;
        this.createCustomerUseCase = createCustomerUseCase;
    }

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(@Valid CreateCustomerRequest request) {
        CustomerResponse response = createCustomerUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        return mapper.toResponse(
                customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Customer.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> listCustomers(CustomerSearchCriteria criteria, Pageable pageable) {
        return customerRepository
                .findAll(CustomerSpecification.customers(criteria), pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<CustomerSnapshot> findCustomerById(UUID id) {
        return customerRepository.findById(id).map(mapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<CustomerSnapshot> findCustomerByDocument(String document) {
        return customerRepository
                .findByDocument(Customer.normalizeDocument(document))
                .map(mapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<SupplierSnapshot> findSupplierById(UUID id) {
        return supplierRepository.findById(id).map(mapper::toSnapshot);
    }
}
