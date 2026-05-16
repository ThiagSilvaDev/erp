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

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.customer.internal.dto.*;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.model.Customer;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.repository.CustomerRepository;

@RestController
class ContactPersonController implements ContactPersonApi {

    private final ContactPersonRepository contactPersonRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;

    ContactPersonController(
            ContactPersonRepository contactPersonRepository,
            CustomerRepository customerRepository,
            CustomerMapper mapper) {
        this.contactPersonRepository = contactPersonRepository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ResponseEntity<ContactPersonResponse> createContactPerson(@Valid CreateContactPersonRequest request) {
        customerRepository
                .findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(Customer.class, request.customerId()));
        ContactPersonResponse response =
                mapper.toResponse(contactPersonRepository.save(mapper.toContactPerson(request)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ContactPersonResponse> getContactPersonById(UUID id) {
        return ResponseEntity.ok(mapper.toResponse(contactPersonRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ContactPerson.class, id))));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ContactPersonResponse>> listContactPersons(Pageable pageable) {
        return ResponseEntity.ok(contactPersonRepository.findAll(pageable).map(mapper::toResponse));
    }
}
