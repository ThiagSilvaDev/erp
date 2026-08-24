package com.thiagsilvadev.erpapi.company.internal.usecase;

import static com.thiagsilvadev.erpapi.company.api.CompanyPermissions.READ;

import java.util.UUID;

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
public class GetCompanyContactByIdUseCase {

    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public GetCompanyContactByIdUseCase(
            CompanyContactRepository companyContactRepository, CompanyMapper companyMapper) {
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public CompanyContactResponse execute(UUID id) {
        return companyMapper.toResponse(companyContactRepository
                .findByIdAndCompanyId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> new ResourceNotFoundException(CompanyContact.class, id)));
    }
}
