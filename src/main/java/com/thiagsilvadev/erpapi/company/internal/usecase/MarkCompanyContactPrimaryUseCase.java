package com.thiagsilvadev.erpapi.company.internal.usecase;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;

@Component
public class MarkCompanyContactPrimaryUseCase {

    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public MarkCompanyContactPrimaryUseCase(
            CompanyContactRepository companyContactRepository, CompanyMapper companyMapper) {
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.company.api.CompanyPermissions).WRITE)")
    public CompanyContactResponse execute(UUID id) {
        CompanyContact companyContact = companyContactRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CompanyContact.class, id));

        companyContactRepository
                .findByCompanyIdAndPrimaryContactTrue(companyContact.getCompanyId())
                .filter(primaryContact -> !primaryContact.getId().equals(companyContact.getId()))
                .ifPresent(CompanyContact::unmarkPrimary);

        companyContact.markPrimary();

        return companyMapper.toResponse(companyContact);
    }
}
