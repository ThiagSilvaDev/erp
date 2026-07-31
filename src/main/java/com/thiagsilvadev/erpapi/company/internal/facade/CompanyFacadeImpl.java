package com.thiagsilvadev.erpapi.company.internal.facade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
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
