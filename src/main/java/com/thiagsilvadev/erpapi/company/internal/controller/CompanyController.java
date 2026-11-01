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
import com.thiagsilvadev.erpapi.company.internal.dto.UpdateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.usecase.CompanyQueryUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.DeleteCompanyUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.GetCompanyAddressUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.UpdateCompanyUseCase;

@RestController
@Validated
class CompanyController implements CompanyApi {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeleteCompanyUseCase deleteCompanyUseCase;
    private final GetCompanyAddressUseCase getCompanyAddressUseCase;
    private final CompanyQueryUseCase companyQueryUseCase;

    CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            UpdateCompanyUseCase updateCompanyUseCase,
            DeleteCompanyUseCase deleteCompanyUseCase,
            GetCompanyAddressUseCase getCompanyAddressUseCase,
            CompanyQueryUseCase companyQueryUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.deleteCompanyUseCase = deleteCompanyUseCase;
        this.getCompanyAddressUseCase = getCompanyAddressUseCase;
        this.companyQueryUseCase = companyQueryUseCase;
    }

    @Override
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = createCompanyUseCase.execute(request);
        return ResponseEntities.created(response.id(), response);
    }

    @Override
    public CompanyResponse updateCompany(@PathVariable UUID id, @Valid @RequestBody UpdateCompanyRequest request) {
        return updateCompanyUseCase.execute(id, request);
    }

    @Override
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        deleteCompanyUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public CompanyResponse getCompanyById(@PathVariable UUID id) {
        return companyQueryUseCase.getById(id);
    }

    @Override
    public CompanyAddressResponse getCompanyAddress(@PathVariable UUID id) {
        return getCompanyAddressUseCase.execute(id);
    }

    @Override
    public Page<CompanyResponse> listCompanies(CompanySearchCriteria criteria, Pageable pageable) {
        return companyQueryUseCase.list(criteria, pageable);
    }
}
