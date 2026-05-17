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
    private final GetContactPersonByIdUseCase getContactPersonByIdUseCase;
    private final ListContactPersonsUseCase listContactPersonsUseCase;

    ContactPersonController(
            CreateContactPersonUseCase createContactPersonUseCase,
            GetContactPersonByIdUseCase getContactPersonByIdUseCase,
            ListContactPersonsUseCase listContactPersonsUseCase) {
        this.createContactPersonUseCase = createContactPersonUseCase;
        this.getContactPersonByIdUseCase = getContactPersonByIdUseCase;
        this.listContactPersonsUseCase = listContactPersonsUseCase;
    }

    @Override
    public ResponseEntity<ContactPersonResponse> createContactPerson(@Valid CreateContactPersonRequest request) {
        ContactPersonResponse response = createContactPersonUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public ContactPersonResponse getContactPersonById(UUID id) {
        return getContactPersonByIdUseCase.execute(id);
    }

    @Override
    public Page<ContactPersonResponse> listContactPersons(Pageable pageable) {
        return listContactPersonsUseCase.execute(pageable);
    }
}
