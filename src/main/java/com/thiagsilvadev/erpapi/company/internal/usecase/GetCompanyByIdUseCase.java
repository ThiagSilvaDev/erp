package com.thiagsilvadev.erpapi.company.internal.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class GetCompanyByIdUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public GetCompanyByIdUseCase(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Transactional(readOnly = true)
    public CompanyResponse execute(UUID id) {
        Company company =
                companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(Company.class, id));

        return companyMapper.toResponse(company);
    }
}
