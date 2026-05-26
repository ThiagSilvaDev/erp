package com.thiagsilvadev.erpapi.company.internal.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.thiagsilvadev.erpapi.common.web.ResponseEntities;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyAddressResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanySearchCriteria;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.GetCompanyAddressUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.GetCompanyByIdUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.ListCompaniesUseCase;

@RestController
@Validated
class CompanyController implements CompanyApi {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final GetCompanyAddressUseCase getCompanyAddressUseCase;
    private final ListCompaniesUseCase listCompaniesUseCase;

    CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            GetCompanyByIdUseCase getCompanyByIdUseCase,
            GetCompanyAddressUseCase getCompanyAddressUseCase,
            ListCompaniesUseCase listCompaniesUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.getCompanyAddressUseCase = getCompanyAddressUseCase;
        this.listCompaniesUseCase = listCompaniesUseCase;
    }

    @Override
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = createCompanyUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public CompanyResponse getCompanyById(@PathVariable UUID id) {
        return getCompanyByIdUseCase.execute(id);
    }

    @Override
    public CompanyAddressResponse getCompanyAddress(@PathVariable UUID id) {
        return getCompanyAddressUseCase.execute(id);
    }

    @Override
    public Page<CompanyResponse> listCompanies(CompanySearchCriteria criteria, Pageable pageable) {
        return listCompaniesUseCase.execute(criteria, pageable);
    }
}
