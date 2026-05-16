package com.thiagsilvadev.erpapi.customer.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.api.*;
import com.thiagsilvadev.erpapi.customer.internal.dto.*;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerSpecification;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@RestController
class CustomerController implements CustomerApi, CustomerFacade {

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final CompanyFacade companyFacade;
    private final CustomerMapper mapper;

    CustomerController(
            CustomerRepository customerRepository,
            SupplierRepository supplierRepository,
            CompanyFacade companyFacade,
            CustomerMapper mapper) {
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
        this.companyFacade = companyFacade;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ResponseEntity<CustomerResponse> createCustomer(@Valid CreateCustomerRequest request) {
        companyFacade
                .findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.companyId()));
        String document = Customer.normalizeDocument(request.document());
        if (customerRepository.existsByDocument(document)) {
            throw new ConflictException(
                    "Customer with this document already exists.", java.util.Map.of("document", document));
        }
        CustomerResponse response = mapper.toResponse(customerRepository.save(mapper.toCustomer(request)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomerResponse> getCustomerById(UUID id) {
        return ResponseEntity.ok(mapper.toResponse(
                customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Customer.class, id))));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Page<CustomerResponse>> listCustomers(CustomerSearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(customerRepository
                .findAll(CustomerSpecification.customers(criteria), pageable)
                .map(mapper::toResponse));
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
