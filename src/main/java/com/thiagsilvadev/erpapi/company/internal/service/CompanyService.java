package com.thiagsilvadev.erpapi.company.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.company.api.CompanySnapshot;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanyResponse;
import com.thiagsilvadev.erpapi.company.internal.dto.CompanySearchCriteria;
import com.thiagsilvadev.erpapi.company.internal.dto.CreateCompanyRequest;
import com.thiagsilvadev.erpapi.company.internal.usecase.*;

@Service
public class CompanyService implements CompanyFacade {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final GetCompanyByIdUseCase getCompanyByIdUseCase;
    private final ListCompaniesUseCase listCompaniesUseCase;
    private final FindCompanySnapshotByIdUseCase findCompanySnapshotByIdUseCase;
    private final FindCompanySnapshotByCnpjUseCase findCompanySnapshotByCnpjUseCase;

    public CompanyService(
            CreateCompanyUseCase createCompanyUseCase,
            GetCompanyByIdUseCase getCompanyByIdUseCase,
            ListCompaniesUseCase listCompaniesUseCase,
            FindCompanySnapshotByIdUseCase findCompanySnapshotByIdUseCase,
            FindCompanySnapshotByCnpjUseCase findCompanySnapshotByCnpjUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.getCompanyByIdUseCase = getCompanyByIdUseCase;
        this.listCompaniesUseCase = listCompaniesUseCase;
        this.findCompanySnapshotByIdUseCase = findCompanySnapshotByIdUseCase;
        this.findCompanySnapshotByCnpjUseCase = findCompanySnapshotByCnpjUseCase;
    }

    public CompanyResponse createCompany(CreateCompanyRequest request) {
        return createCompanyUseCase.execute(request);
    }

    public CompanyResponse getCompanyById(UUID id) {
        return getCompanyByIdUseCase.execute(id);
    }

    public Page<CompanyResponse> listCompanies(CompanySearchCriteria criteria, Pageable pageable) {
        return listCompaniesUseCase.execute(criteria, pageable);
    }

    @Override
    public Optional<CompanySnapshot> findById(UUID id) {
        return findCompanySnapshotByIdUseCase.execute(id);
    }

    @Override
    public Optional<CompanySnapshot> findByCnpj(String cnpj) {
        return findCompanySnapshotByCnpjUseCase.execute(cnpj);
    }
}
