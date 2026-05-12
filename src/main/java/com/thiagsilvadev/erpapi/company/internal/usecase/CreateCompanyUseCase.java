package com.thiagsilvadev.erpapi.company.internal.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.model.Cnpj;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.event.CompanyEventPublisher;
import com.thiagsilvadev.erpapi.company.internal.exception.CnpjAlreadyExistsException;
import com.thiagsilvadev.erpapi.company.internal.mapper.CompanyMapper;
import com.thiagsilvadev.erpapi.company.internal.model.Company;
import com.thiagsilvadev.erpapi.company.internal.repository.CompanyRepository;

@Component
public class CreateCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final CompanyEventPublisher eventPublisher;

    public CreateCompanyUseCase(
            CompanyRepository companyRepository, CompanyMapper companyMapper, CompanyEventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CompanyResponse execute(CreateCompanyRequest request) {
        Cnpj cnpj = Cnpj.create(request.cnpj());

        if (companyRepository.existsByCnpj(cnpj)) {
            throw new CnpjAlreadyExistsException(cnpj.getNumber());
        }

        Company company = companyMapper.toEntity(request, cnpj);
        Company savedCompany = companyRepository.save(company);

        eventPublisher.companyCreated(savedCompany);

        return companyMapper.toResponse(savedCompany);
    }
}
