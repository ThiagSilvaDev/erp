package com.thiagsilvadev.erpapi.customer.internal.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ConflictException;
import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.company.api.CompanyFacade;
import com.thiagsilvadev.erpapi.customer.internal.dto.CreateSupplierRequest;
import com.thiagsilvadev.erpapi.customer.internal.dto.CustomerMapper;
import com.thiagsilvadev.erpapi.customer.internal.dto.SupplierResponse;
import com.thiagsilvadev.erpapi.customer.internal.model.Supplier;
import com.thiagsilvadev.erpapi.customer.internal.repository.SupplierRepository;

@Component
public class CreateSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final CompanyFacade companyFacade;
    private final CustomerMapper mapper;

    public CreateSupplierUseCase(
            SupplierRepository supplierRepository, CompanyFacade companyFacade, CustomerMapper mapper) {
        this.supplierRepository = supplierRepository;
        this.companyFacade = companyFacade;
        this.mapper = mapper;
    }

    @Transactional
    public SupplierResponse execute(CreateSupplierRequest request) {
        companyFacade
                .findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(Object.class, request.companyId()));

        String document = Supplier.normalizeDocument(request.document());
        if (supplierRepository.existsByDocument(document)) {
            throw new ConflictException(
                    "Supplier with this document already exists.", java.util.Map.of("document", document));
        }

        Supplier supplier = Supplier.create(
                request.name(),
                request.document(),
                request.companyId(),
                mapper.toContact(request.contact()),
                mapper.toAddress(request.address()));

        return mapper.toResponse(supplierRepository.save(supplier));
    }
}
