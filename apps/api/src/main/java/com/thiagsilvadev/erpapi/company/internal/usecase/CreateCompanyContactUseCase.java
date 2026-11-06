package com.thiagsilvadev.erpapi.company.internal.usecase;

import static com.thiagsilvadev.erpapi.company.api.CompanyPermissions.WRITE;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.validation.Strings;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyContactResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyContactRequest;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class CreateCompanyContactUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public CreateCompanyContactUseCase(
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional
    @PreAuthorize("hasAuthority('" + WRITE + "')")
    public CompanyContactResponse execute(CreateCompanyContactRequest request) {
        var companyId = TenantContext.requireTenantId();
        companyRepository
                .findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException(Company.class, companyId));

        boolean firstContact = !companyContactRepository.existsByCompanyId(companyId);
        boolean shouldBePrimary = firstContact || Boolean.TRUE.equals(request.primaryContact());

        CompanyContact companyContact = CompanyContact.create(
                companyId,
                Strings.trimToNull(request.name()),
                Strings.trimToNull(request.role()),
                companyMapper.toContact(request.contact()),
                shouldBePrimary);

        if (shouldBePrimary) {
            companyContactRepository
                    .findByCompanyIdAndPrimaryContactTrue(companyId)
                    .ifPresent(CompanyContact::unmarkPrimary);
        }

        return companyMapper.toResponse(companyContactRepository.save(companyContact));
    }
}
