package com.thiagsilvadev.erpapi.company.internal.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanySearchCriteria;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.usecase.CreateCompanyUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.GetCompanyByIdUseCase;
import com.thiagsilvadev.erpapi.company.internal.usecase.ListCompaniesUseCase;

@RestController
@RequestMapping("/api/v1/company")
@Validated
class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final ListCompaniesUseCase listCompaniesUseCase;

    CompanyController(
            CreateCompanyUseCase createCompanyUseCase,
            GetCompanyByIdUseCase getCompanyByIdUseCase,
            ListCompaniesUseCase listCompaniesUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.listCompaniesUseCase = listCompaniesUseCase;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        CompanyResponse response = createCompanyUseCase.execute(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable UUID id) {
        return ResponseEntity.ok(getCompanyByIdUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> listCompanies(CompanySearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(listCompaniesUseCase.execute(criteria, pageable));
    }
}
