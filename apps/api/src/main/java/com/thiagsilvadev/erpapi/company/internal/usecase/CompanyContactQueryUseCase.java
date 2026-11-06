package com.thiagsilvadev.erpapi.company.internal.usecase;

import static com.thiagsilvadev.erpapi.company.api.CompanyPermissions.READ;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;

@Component
public class CompanyContactQueryUseCase {

    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public CompanyContactQueryUseCase(CompanyContactRepository companyContactRepository, CompanyMapper companyMapper) {
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public CompanyContactResponse getById(UUID id) {
        return companyMapper.toResponse(companyContactRepository
                .findByIdAndCompanyId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException(CompanyContact.class, id)));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<CompanyContactResponse> list(Boolean primaryContact, Pageable pageable) {
        UUID companyId = TenantContext.requireTenantId();
        if (primaryContact != null) {
            return companyContactRepository
                    .findByCompanyIdAndPrimaryContact(companyId, primaryContact, pageable)
                    .map(companyMapper::toResponse);
        }
        return companyContactRepository.findByCompanyId(companyId, pageable).map(companyMapper::toResponse);
    }
}
