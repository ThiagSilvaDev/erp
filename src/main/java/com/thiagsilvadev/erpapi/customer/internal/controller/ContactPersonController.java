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
import com.thiagsilvadev.erpapi.customer.internal.dto.*;
import com.thiagsilvadev.erpapi.customer.internal.model.ContactPerson;
import com.thiagsilvadev.erpapi.customer.internal.repository.ContactPersonRepository;
import com.thiagsilvadev.erpapi.customer.internal.usecase.CreateContactPersonUseCase;

@RestController
class ContactPersonController implements ContactPersonApi {

    private final ContactPersonRepository contactPersonRepository;
    private final CustomerMapper mapper;
    private final CreateContactPersonUseCase createContactPersonUseCase;

    ContactPersonController(
            ContactPersonRepository contactPersonRepository,
            CustomerMapper mapper,
            CreateContactPersonUseCase createContactPersonUseCase) {
        this.contactPersonRepository = contactPersonRepository;
        this.mapper = mapper;
        this.createContactPersonUseCase = createContactPersonUseCase;
    }

    @Override
    public ResponseEntity<ContactPersonResponse> createContactPerson(@Valid CreateContactPersonRequest request) {
        ContactPersonResponse response = createContactPersonUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactPersonResponse getContactPersonById(UUID id) {
        return mapper.toResponse(contactPersonRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ContactPerson.class, id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContactPersonResponse> listContactPersons(Pageable pageable) {
        return contactPersonRepository.findAll(pageable).map(mapper::toResponse);
    }
}
