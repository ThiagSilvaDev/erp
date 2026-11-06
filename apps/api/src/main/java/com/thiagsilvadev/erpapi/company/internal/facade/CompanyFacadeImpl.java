package com.thiagsilvadev.erpapi.company.internal.facade;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.model.CompanyContact;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Service
public class CompanyFacadeImpl implements CompanyFacade {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public CompanyFacadeImpl(
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanySnapshot getCompany(UUID companyId) {
        return companyRepository
                .findById(companyId)
                .map(company -> companyMapper.toSnapshot(
                        company,
                        companyContactRepository
                                .findByCompanyIdAndPrimaryContactTrue(companyId)
                                .orElse(null)))
                .orElseThrow(() -> new ResourceNotFoundException(Company.class, companyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanySnapshot> getCompanies(Set<UUID> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) {
            return List.of();
        }

        List<Company> companies = companyRepository.findAllById(companyIds);
        Set<UUID> missingIds = new TreeSet<>(companyIds);
        companies.forEach(company -> missingIds.remove(company.getId()));
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException(Company.class, missingIds);
        }

        Map<UUID, CompanyContact> primaryContacts =
                companyContactRepository.findByCompanyIdInAndPrimaryContactTrue(companyIds).stream()
                        .collect(Collectors.toMap(CompanyContact::getCompanyId, Function.identity()));

        return companies.stream()
                .map(company -> companyMapper.toSnapshot(company, primaryContacts.get(company.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CompanySnapshot resolveCompany(String cnpj) {
        return companyRepository
                .findByCnpj(Cnpj.create(cnpj))
                .map(company -> companyMapper.toSnapshot(
                        company,
                        companyContactRepository
                                .findByCompanyIdAndPrimaryContactTrue(company.getId())
                                .orElse(null)))
                .orElseThrow(() -> new ResourceNotFoundException(Company.class, cnpj));
    }
}
