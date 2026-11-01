package com.thiagsilvadev.erpapi.company.internal.usecase;

import static com.thiagsilvadev.erpapi.company.api.CompanyPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyAddressResponse;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class GetCompanyAddressUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public GetCompanyAddressUseCase(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public CompanyAddressResponse execute(UUID companyId) {
        Company company = companyRepository
                .findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Company.class, companyId));

        return companyMapper.toAddressResponse(company);
    }
}
