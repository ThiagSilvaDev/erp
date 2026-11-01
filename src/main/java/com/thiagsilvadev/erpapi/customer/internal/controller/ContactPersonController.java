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
class ContactPersonController implements ContactPersonApi {

    private final CreateContactPersonUseCase createContactPersonUseCase;
    private final ContactPersonQueryUseCase contactPersonQueryUseCase;

    ContactPersonController(
            CreateContactPersonUseCase createContactPersonUseCase,
            ContactPersonQueryUseCase contactPersonQueryUseCase) {
        this.createContactPersonUseCase = createContactPersonUseCase;
        this.contactPersonQueryUseCase = contactPersonQueryUseCase;
    }

    @Override
    public ResponseEntity<ContactPersonResponse> create(@Valid CreateContactPersonRequest request) {
        ContactPersonResponse response = createContactPersonUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public ContactPersonResponse getById(UUID id) {
        return contactPersonQueryUseCase.getById(id);
    }

    @Override
    public Page<ContactPersonResponse> list(Pageable pageable) {
        return contactPersonQueryUseCase.list(pageable);
    }
}
