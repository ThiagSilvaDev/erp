package com.thiagsilvadev.erpapi.company.internal.usecase;

import static com.thiagsilvadev.erpapi.company.api.CompanyPermissions.READ;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.AbstractBaseEntity;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanySearchCriteria;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanySpecification;

@Component
public class CompanyQueryUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public CompanyQueryUseCase(
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public CompanyResponse getById(UUID id) {
        Company company =
                companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Company.class, id));

        return companyMapper.toResponse(
                company,
                companyContactRepository
                        .findByCompanyIdAndPrimaryContactTrue(id)
                        .orElse(null));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<CompanyResponse> list(CompanySearchCriteria criteria, Pageable pageable) {
        var companies = companyRepository.findAll(CompanySpecification.withCriteria(criteria), pageable);
        Map<UUID, CompanyContact> primaryContactsByCompanyId =
                companyContactRepository
                        .findByCompanyIdInAndPrimaryContactTrue(
                                companies.map(AbstractBaseEntity::getId).toList())
                        .stream()
                        .collect(Collectors.toMap(CompanyContact::getCompanyId, companyContact -> companyContact));

        return companies.map(
                company -> companyMapper.toResponse(company, primaryContactsByCompanyId.get(company.getId())));
    }
}
