package com.thiagsilvadev.erpapi.company.internal.usecase;

import static com.thiagsilvadev.erpapi.company.api.CompanyPermissions.WRITE;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.UpdateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class UpdateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public UpdateCompanyUseCase(
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public CompanyResponse execute(UUID id, UpdateCompanyRequest request) {
        Company company =
                companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Company.class, id));

        company.updateNames(request.legalName(), request.tradeName());
        company.changeType(request.type());
        company.changeAddress(companyMapper.toAddress(request.address()));
        company.changeContact(companyMapper.toContact(request.contact()));

        return companyMapper.toResponse(
                company,
                companyContactRepository
                        .findByCompanyIdAndPrimaryContactTrue(id)
                        .orElse(null));
    }
}
