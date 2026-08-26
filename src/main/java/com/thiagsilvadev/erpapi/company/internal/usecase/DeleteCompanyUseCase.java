package com.thiagsilvadev.erpapi.company.internal.usecase;

import static com.thiagsilvadev.erpapi.company.api.CompanyPermissions.DELETE;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class DeleteCompanyUseCase {

    private final CompanyRepository companyRepository;

    public DeleteCompanyUseCase(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + DELETE + "')")
    public void execute(UUID id) {
        Company company =
                companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Company.class, id));

        company.inactivate();
    }
}
