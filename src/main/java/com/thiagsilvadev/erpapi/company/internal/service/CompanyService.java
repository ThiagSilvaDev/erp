package com.thiagsilvadev.erpapi.company.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.company.api.CompanyAddressSnapshot;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyContactRepository;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Service
public class CompanyService implements CompanyFacade {

    private final CompanyRepository companyRepository;
    private final CompanyContactRepository companyContactRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(
            CompanyRepository companyRepository,
            CompanyContactRepository companyContactRepository,
            CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyContactRepository = companyContactRepository;
        this.companyMapper = companyMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanySnapshot> findById(UUID id) {
        return companyRepository
                .findById(id)
                .map(company -> companyMapper.toSnapshot(
                        company,
                        companyContactRepository
                                .findByCompanyIdAndPrimaryContactTrue(id)
                                .orElse(null)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanySnapshot> findByCnpj(String cnpj) {
        return companyRepository
                .findByCnpj(Cnpj.create(cnpj))
                .map(company -> companyMapper.toSnapshot(
                        company,
                        companyContactRepository
                                .findByCompanyIdAndPrimaryContactTrue(company.getId())
                                .orElse(null)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyAddressSnapshot> findAddressByCompanyId(UUID id) {
        return companyRepository.findById(id).map(companyMapper::toAddressSnapshot);
    }
}
