package com.thiagsilvadev.erpapi.company.internal.usecase;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberService;
import com.thiagsilvadev.erpapi.common.domain.numbering.BusinessNumberType;
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
    private final BusinessNumberService businessNumberService;

    public CreateCompanyUseCase(
            CompanyRepository companyRepository,
            CompanyMapper companyMapper,
            CompanyEventPublisher eventPublisher,
            BusinessNumberService businessNumberService) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
        this.eventPublisher = eventPublisher;
        this.businessNumberService = businessNumberService;
    }

    @Transactional
    @PreAuthorize("hasAuthority(T(com.thiagsilvadev.erpapi.company.api.CompanyPermissions).WRITE)")
    public CompanyResponse execute(CreateCompanyRequest request) {
        var cnpj = companyMapper.toCnpj(request.cnpj());

        if (companyRepository.existsByCnpj(cnpj)) {
            throw new CnpjAlreadyExistsException(cnpj.getNumber());
        }

        Company company = Company.create(
                businessNumberService.next(BusinessNumberType.COMPANY),
                companyMapper.trimToNull(request.legalName()),
                companyMapper.trimToNull(request.tradeName()),
                cnpj,
                request.type(),
                companyMapper.toAddress(request.address()),
                companyMapper.toContact(request.contact()));
        Company savedCompany;
        try {
            savedCompany = companyRepository.saveAndFlush(company);
        } catch (DataIntegrityViolationException e) {
            throw new CnpjAlreadyExistsException(cnpj.getNumber());
        }

        eventPublisher.companyCreated(savedCompany);

        return companyMapper.toResponse(savedCompany);
    }
}
