package com.thiagsilvadev.erpapi.company.internal.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        var cnpj = companyMapper.toCnpj(request.cnpj());

        if (companyRepository.existsByCnpj(cnpj)) {
            throw new CnpjAlreadyExistsException(cnpj.getNumber());
        }

        Company company = Company.create(
                companyMapper.trimToNull(request.legalName()),
                companyMapper.trimToNull(request.tradeName()),
                cnpj,
                request.type(),
                companyMapper.toAddress(request.address()),
                companyMapper.toContact(request.contact()));
        Company savedCompany = companyRepository.save(company);

        eventPublisher.companyCreated(savedCompany);

        return companyMapper.toResponse(savedCompany);
    }
}
