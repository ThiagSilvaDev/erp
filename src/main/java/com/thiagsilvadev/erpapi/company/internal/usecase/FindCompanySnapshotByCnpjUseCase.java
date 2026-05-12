package com.thiagsilvadev.erpapi.company.internal.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.model.Cnpj;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class FindCompanySnapshotByCnpjUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public FindCompanySnapshotByCnpjUseCase(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    public Optional<CompanySnapshot> execute(String cnpj) {
        return companyRepository.findByCnpj(Cnpj.create(cnpj)).map(companyMapper::toSnapshot);
    }
}
