package com.thiagsilvadev.erpapi.company.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyContactRequest;
import com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyContactUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.GetCompanyContactByIdUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.ListCompanyContactsUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.MarkCompanyContactPrimaryUseCase;

@RestController
@Validated
class CompanyContactController implements CompanyContactApi {

    private final CreateCompanyContactUseCase createCompanyContactUseCase;
    private final GetCompanyContactByIdUseCase getCompanyContactByIdUseCase;
    private final ListCompanyContactsUseCase listCompanyContactsUseCase;
    private final MarkCompanyContactPrimaryUseCase markCompanyContactPrimaryUseCase;

    CompanyContactController(
            CreateCompanyContactUseCase createCompanyContactUseCase,
            GetCompanyContactByIdUseCase getCompanyContactByIdUseCase,
            ListCompanyContactsUseCase listCompanyContactsUseCase,
            MarkCompanyContactPrimaryUseCase markCompanyContactPrimaryUseCase) {
        this.createCompanyContactUseCase = createCompanyContactUseCase;
        this.getCompanyContactByIdUseCase = getCompanyContactByIdUseCase;
        this.listCompanyContactsUseCase = listCompanyContactsUseCase;
        this.markCompanyContactPrimaryUseCase = markCompanyContactPrimaryUseCase;
    }

    @Override
    public ResponseEntity<CompanyContactResponse> createCompanyContact(
            @Valid @RequestBody CreateCompanyContactRequest request) {
        CompanyContactResponse response = createCompanyContactUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public CompanyContactResponse getCompanyContactById(@PathVariable UUID id) {
        return getCompanyContactByIdUseCase.execute(id);
    }

    @Override
    public Page<CompanyContactResponse> listCompanyContacts(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) Boolean primaryContact,
            Pageable pageable) {
        return listCompanyContactsUseCase.execute(companyId, primaryContact, pageable);
    }

    @Override
    public CompanyContactResponse markCompanyContactPrimary(@PathVariable UUID id) {
        return markCompanyContactPrimaryUseCase.execute(id);
    }
}
