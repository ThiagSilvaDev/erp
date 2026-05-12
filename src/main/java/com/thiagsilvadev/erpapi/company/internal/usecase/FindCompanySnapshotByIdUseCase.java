package com.thiagsilvadev.erpapi.company.internal.usecase;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class FindCompanySnapshotByIdUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public FindCompanySnapshotByIdUseCase(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    public Optional<CompanySnapshot> execute(UUID id) {
        return companyRepository.findById(id).map(companyMapper::toSnapshot);
    }
}
