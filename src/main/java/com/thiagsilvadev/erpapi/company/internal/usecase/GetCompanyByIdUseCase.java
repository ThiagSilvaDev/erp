package com.thiagsilvadev.erpapi.company.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class GetCompanyByIdUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public GetCompanyByIdUseCase(
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.company.api.CompanyPermissions).READ)")
    public CompanyResponse execute(UUID id) {
        Company company =
                companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Company.class, id));

        return companyMapper.toResponse(
                company,
                companyContactRepository
                        .findByCompanyIdAndPrimaryContactTrue(id)
                        .orElse(null));
    }
}
