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
import com.thiagsilvadev.erpapi.company.internal.usecase.CompanyContactQueryUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyContactUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.MarkCompanyContactPrimaryUseCase;

@RestController
@Validated
class CompanyContactController implements CompanyContactApi {

    private final CreateCompanyContactUseCase createCompanyContactUseCase;
    private final CompanyContactQueryUseCase companyContactQueryUseCase;
    private final MarkCompanyContactPrimaryUseCase markCompanyContactPrimaryUseCase;

    CompanyContactController(
            CreateCompanyContactUseCase createCompanyContactUseCase,
            CompanyContactQueryUseCase companyContactQueryUseCase,
            MarkCompanyContactPrimaryUseCase markCompanyContactPrimaryUseCase) {
        this.createCompanyContactUseCase = createCompanyContactUseCase;
        this.companyContactQueryUseCase = companyContactQueryUseCase;
        this.markCompanyContactPrimaryUseCase = markCompanyContactPrimaryUseCase;
    }

    @Override
    public ResponseEntity<CompanyContactResponse> create(@Valid @RequestBody CreateCompanyContactRequest request) {
        CompanyContactResponse response = createCompanyContactUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public CompanyContactResponse getById(@PathVariable UUID id) {
        return companyContactQueryUseCase.getById(id);
    }

    @Override
    public Page<CompanyContactResponse> list(
            @RequestParam(required = false) Boolean primaryContact, Pageable pageable) {
        return companyContactQueryUseCase.list(primaryContact, pageable);
    }

    @Override
    public CompanyContactResponse markPrimary(@PathVariable UUID id) {
        return markCompanyContactPrimaryUseCase.execute(id);
    }
}
