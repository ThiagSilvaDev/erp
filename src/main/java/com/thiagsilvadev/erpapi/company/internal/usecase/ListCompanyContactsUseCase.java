package com.thiagsilvadev.erpapi.company.internal.usecase;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;

@Component
public class ListCompanyContactsUseCase {

    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public ListCompanyContactsUseCase(CompanyContactRepository companyContactRepository, CompanyMapper companyMapper) {
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.company.api.CompanyPermissions).READ)")
    public Page<CompanyContactResponse> execute(UUID companyId, Boolean primaryContact, Pageable pageable) {
        if (companyId != null && primaryContact != null) {
            return companyContactRepository
                    .findByCompanyIdAndPrimaryContact(companyId, primaryContact, pageable)
                    .map(companyMapper::toResponse);
        }
        if (companyId != null) {
            return companyContactRepository.findByCompanyId(companyId, pageable).map(companyMapper::toResponse);
        }
        if (primaryContact != null) {
            return companyContactRepository
                    .findByPrimaryContact(primaryContact, pageable)
                    .map(companyMapper::toResponse);
        }

        return companyContactRepository.findAll(pageable).map(companyMapper::toResponse);
    }
}
