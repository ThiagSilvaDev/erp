package com.thiagsilvadev.erpapi.company.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Cnpj;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Service
public class CompanyService implements CompanyFacade {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanySnapshot> findById(UUID id) {
        return companyRepository.findById(id).map(companyMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanySnapshot> findByCnpj(String cnpj) {
        return companyRepository.findByCnpj(Cnpj.create(cnpj)).map(companyMapper::toSnapshot);
    }
}
