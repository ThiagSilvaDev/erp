package com.thiagsilvadev.erpapi.company.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanySearchCriteria;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanySpecification;

@Component
public class ListCompaniesUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public ListCompaniesUseCase(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.company.api.CompanyPermissions).READ)")
    public Page<CompanyResponse> execute(CompanySearchCriteria criteria, Pageable pageable) {
        return companyRepository
                .findAll(CompanySpecification.withCriteria(criteria), pageable)
                .map(companyMapper::toResponse);
    }
}
